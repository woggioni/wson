package net.woggioni.wson.wcfg;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.woggioni.jwo.JWO;
import net.woggioni.wson.value.ArrayValue;
import net.woggioni.wson.value.BooleanValue;
import net.woggioni.wson.value.FloatValue;
import net.woggioni.wson.value.IntegerValue;
import net.woggioni.wson.value.NullValue;
import net.woggioni.wson.value.ObjectValue;
import net.woggioni.wson.value.StringValue;
import net.woggioni.wson.xface.Value;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static net.woggioni.jwo.JWO.newThrowable;
import static net.woggioni.jwo.JWO.tail;

class ListenerImpl implements WCFGListener {

    private final Value.Configuration cfg;

    @Getter
    private Value result;

    private final Map<String, ValueHolder> unresolvedReferences = new TreeMap<>();

    private interface StackLevel {
        Value getValue();
    }

    @RequiredArgsConstructor
    private static class ExportStackLevel implements StackLevel {

        @Setter
        private Value value;

        @Override
        public Value getValue() {
            return value;
        }
    }

    private static class ArrayStackLevel implements StackLevel {
        private final ArrayValue value = new ArrayValue();

        @Override
        public Value getValue() {
            return value;
        }
    }

    private static class ObjectStackLevel implements StackLevel {
        public String currentKey;
        private final ObjectValue value;

        public ObjectStackLevel(Value.Configuration cfg) {
            value = ObjectValue.newInstance(cfg);
        }

        @Override
        public Value getValue() {
            return value;
        }
    }

    private static class ExpressionStackLevel implements StackLevel {
        private final Value.Configuration cfg;
        private ObjectValue value = null;
        public List<Value> elements = new ArrayList<>();

        public ExpressionStackLevel(Value.Configuration cfg) {
            this.cfg = cfg;
        }

        @Override
        public Value getValue() {
            if (value == null) {
                List<ObjectValue> objects = elements.stream()
                        .map(it -> {
                            if (it instanceof ObjectValue ov)
                                return ov;
                            else if (it instanceof ValueHolder vh) {
                                return (ObjectValue) vh.getDelegate();
                            } else {
                                throw newThrowable(RuntimeException.class, "");
                            }
                        })
                        .collect(Collectors.toList());
                value = new CompositeObjectValue(objects, cfg);
            }
            return value;
        }
    }

    private final List<StackLevel> stack = new ArrayList<>();

    private void add2Last(Value value) {
        StackLevel last = tail(stack);
        if (last instanceof ArrayStackLevel asl) {
            asl.value.add(value);
            if (value instanceof ValueHolder holder) {
                Value arrayValue = asl.getValue();
                int index = arrayValue.size() - 1;
                holder.addDeleter(() -> arrayValue.set(index, holder.getDelegate()));
            }
        } else if (last instanceof ObjectStackLevel osl) {
            String key = osl.currentKey;
            Value objectValue = osl.getValue();
            objectValue.put(key, value);
            if (value instanceof ValueHolder holder) {
                holder.addDeleter(() -> {
                    objectValue.put(key, holder.getDelegate());
                });
            }
        } else if (last instanceof ExpressionStackLevel esl) {
            List<Value> values = esl.elements;
            int index = values.size();
            values.add(value);
            if (value instanceof ValueHolder holder) {
                holder.addDeleter(() -> {
                    values.set(index, holder.getDelegate());
                });
            }
        } else if (last instanceof ExportStackLevel esl) {
            esl.setValue(value);
        }
    }

    private static String unquote(String quoted) {
        return quoted.substring(1, quoted.length() - 1);
    }

    public ListenerImpl(Value.Configuration cfg) {
        this.cfg = cfg;
        StackLevel sl = new ObjectStackLevel(cfg);
        result = sl.getValue();
        stack.add(sl);
    }

    private StackLevel pop() {
        int size = stack.size() - 1;
        StackLevel sl = stack.get(size);
        stack.remove(size);
        return sl;
    }

    @Override
    public void enterWcfg(WCFGParser.WcfgContext ctx) {
    }


    @Override
    public void exitWcfg(WCFGParser.WcfgContext ctx) {
        stack.clear();
        for (ValueHolder holder : unresolvedReferences.values()) {
            if (holder.getDelegate() == null) {
                TerminalNode node = holder.getNode();
                throw new ParseError(
                        "Undeclared identifier '" + node.getText() + "'",
                        node.getSymbol().getLine(),
                        node.getSymbol().getCharPositionInLine() + 1
                );
            }
            holder.replace();
        }
    }

    @Override
    public void enterAssignment(WCFGParser.AssignmentContext ctx) {
        ObjectStackLevel osl = (ObjectStackLevel) stack.get(0);
        String key = ctx.IDENTIFIER().getText();
        osl.currentKey = key;
        ValueHolder holder = unresolvedReferences.computeIfAbsent(key, it -> new ValueHolder(ctx.IDENTIFIER()));
//        holder.addDeleter(() -> holder.setDelegate(result.get(key)));
    }

    @Override
    public void exitAssignment(WCFGParser.AssignmentContext ctx) {
        ObjectStackLevel osl = (ObjectStackLevel) stack.get(0);
        String key = osl.currentKey;
        unresolvedReferences.get(key).setDelegate(osl.getValue().get(key));
        osl.currentKey = null;
    }

    @Override
    public void enterExpression(WCFGParser.ExpressionContext ctx) {
        ExpressionStackLevel esl = new ExpressionStackLevel(cfg);
        stack.add(esl);
        for(TerminalNode node : ctx.IDENTIFIER()) {
            String key = node.getSymbol().getText();
            ValueHolder holder = unresolvedReferences
                    .computeIfAbsent(key, k -> new ValueHolder(node));
            int index = esl.elements.size();
            esl.elements.add(holder);
            holder.addDeleter(() -> {
                esl.elements.set(index, holder.getDelegate());
            });
        }
    }

    @Override
    public void exitExpression(WCFGParser.ExpressionContext ctx) {
        add2Last(pop().getValue());
    }

    @Override
    public void enterObj(WCFGParser.ObjContext ctx) {
        ObjectStackLevel osl = new ObjectStackLevel(cfg);
        stack.add(osl);
    }

    @Override
    public void exitObj(WCFGParser.ObjContext ctx) {
        add2Last(pop().getValue());
    }

    @Override
    public void enterPair(WCFGParser.PairContext ctx) {
        ObjectStackLevel osl = (ObjectStackLevel) stack.get(stack.size() - 1);
        osl.currentKey = unquote(ctx.STRING().getText());
    }

    @Override
    public void exitPair(WCFGParser.PairContext ctx) {
    }

    @Override
    public void enterArray(WCFGParser.ArrayContext ctx) {
        ArrayStackLevel asl = new ArrayStackLevel();
        stack.add(asl);
    }

    @Override
    public void exitArray(WCFGParser.ArrayContext ctx) {
        add2Last(pop().getValue());
    }

    @Override
    public void enterValue(WCFGParser.ValueContext ctx) {
        if (ctx.obj() != null) {
        } else if (ctx.array() != null) {
        } else if (ctx.STRING() != null) {
            add2Last(new StringValue(unquote(ctx.STRING().getText())));
        } else if (ctx.BOOLEAN() != null) {
            add2Last(new BooleanValue(Boolean.parseBoolean(ctx.BOOLEAN().getText())));
        } else if (ctx.NULL() != null) {
            add2Last(new NullValue());
        } else if (ctx.NUMBER() != null) {
            String text = ctx.NUMBER().getText();
            if (text.indexOf('.') < 0) {
                add2Last(new IntegerValue(Long.parseLong(text)));
            } else {
                add2Last(new FloatValue(Double.parseDouble(text)));
            }
        } else if (ctx.IDENTIFIER() != null) {
            String name = ctx.IDENTIFIER().getText();
            ValueHolder referredValue = unresolvedReferences.computeIfAbsent(name,
                    it -> new ValueHolder(ctx.IDENTIFIER())
            );
//            referredValue.addError(() -> new ParseError(
//            "Undeclared identifier '" + name + "'",
//                    ctx.start.getLine(),
//            ctx.start.getCharPositionInLine() + 1
//                )
//            );
//            Value referredValue = result.getOrDefault(name, null);
//            if(referredValue == null) {
//                throw new ParseError(
//            "Undeclared identifier '" + name + "'",
//                    ctx.start.getLine(),
//            ctx.start.getCharPositionInLine() + 1
//                );
//            }
            add2Last(referredValue);
        }
    }

    @Override
    public void exitValue(WCFGParser.ValueContext ctx) {

    }

    @Override
    public void visitTerminal(TerminalNode node) {

    }

    @Override
    public void visitErrorNode(ErrorNode node) {
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {

    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {

    }

    @Override
    public void enterExport(WCFGParser.ExportContext ctx) {
        ExportStackLevel esl = new ExportStackLevel();
        stack.add(esl);
    }

    @Override
    public void exitExport(WCFGParser.ExportContext ctx) {
        result = pop().getValue();
        if (result instanceof ValueHolder holder) {
            holder.addDeleter(() -> {
                result = holder.getDelegate();
            });
        }
    }
}
