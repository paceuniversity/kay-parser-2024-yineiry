package com.scanner.project;

public class ConcreteSyntax {

    public Token token;
    public TokenStream input;

    public ConcreteSyntax(TokenStream ts) {
        input = ts;
        token = input.nextToken();
    }

    private String SyntaxError(String expected) {
        return "Syntax error - Expecting: " + expected +
               " But saw: " + token.getType() + " = " + token.getValue();
    }

    private void match(String s) {
        if (token.getValue().equals(s))
            token = input.nextToken();
        else
            throw new RuntimeException(SyntaxError(s));
    }

    public Program program() {
        Program p = new Program();
        match("main");
        match("{");
        p.decpart = declarations();
        p.body = statements();
        match("}");
        return p;
    }

    private Declarations declarations() {
        Declarations d = new Declarations();
        while (token.getValue().equals("integer") || token.getValue().equals("bool"))
            declaration(d);
        return d;
    }

    private void declaration(Declarations ds) {
        Type t = type();
        identifiers(ds, t);
        match(";");
    }

    private Type type() {
        if (token.getValue().equals("integer") || token.getValue().equals("bool")) {
            Type t = new Type(token.getValue());
            token = input.nextToken();
            return t;
        }
        throw new RuntimeException(SyntaxError("integer | bool"));
    }

    private void identifiers(Declarations ds, Type t) {
        if (!token.getType().equals("Identifier"))
            throw new RuntimeException(SyntaxError("Identifier"));

        while (true) {
            Declaration d = new Declaration();
            d.t = t;
            d.v = new Variable();
            d.v.id = token.getValue();
            ds.addElement(d);
            token = input.nextToken();

            if (!token.getValue().equals(",")) break;
            token = input.nextToken();
        }
    }

    private Block statements() {
        Block b = new Block();
        while (!token.getValue().equals("}"))
            b.blockmembers.add(statement());
        return b;
    }

    private Statement statement() {
        if (token.getValue().equals(";")) {
            token = input.nextToken();
            return new Skip();
        }
        if (token.getValue().equals("{")) {
            token = input.nextToken();
            Block b = statements();
            match("}");
            return b;
        }
        if (token.getValue().equals("if")) return ifStatement();
        if (token.getValue().equals("while")) return whileStatement();
        if (token.getType().equals("Identifier")) return assignment();

        throw new RuntimeException(SyntaxError("Statement"));
    }

    private Assignment assignment() {
        Assignment a = new Assignment();
        a.variable = new Variable();
        a.variable.id = token.getValue();
        token = input.nextToken();
        match(":=");
        a.source = expression();
        match(";");
        return a;
    }

    private Expression expression() {
        Expression e = conjunction();
        while (token.getValue().equals("||")) {
            Binary b = new Binary();
            b.term1 = e;
            b.op = new Operator(token.getValue());
            token = input.nextToken();
            b.term2 = conjunction();
            e = b;
        }
        return e;
    }

    private Expression conjunction() {
        Expression e = relation();
        while (token.getValue().equals("&&")) {
            Binary b = new Binary();
            b.term1 = e;
            b.op = new Operator(token.getValue());
            token = input.nextToken();
            b.term2 = relation();
            e = b;
        }
        return e;
    }

    private Expression relation() {
        Expression e = addition();
        while (token.getValue().matches("<|<=|>|>=|==|!=")) {
            Binary b = new Binary();
            b.term1 = e;
            b.op = new Operator(token.getValue());
            token = input.nextToken();
            b.term2 = addition();
            e = b;
        }
        return e;
    }

    private Expression addition() {
        Expression e = term();
        while (token.getValue().equals("+") || token.getValue().equals("-")) {
            Binary b = new Binary();
            b.term1 = e;
            b.op = new Operator(token.getValue());
            token = input.nextToken();
            b.term2 = term();
            e = b;
        }
        return e;
    }

    private Expression term() {
        Expression e = factor();
        while (token.getValue().equals("*") || token.getValue().equals("/")) {
            Binary b = new Binary();
            b.term1 = e;
            b.op = new Operator(token.getValue());
            token = input.nextToken();
            b.term2 = factor();
            e = b;
        }
        return e;
    }

    private Expression factor() {
        if (token.getType().equals("Identifier")) {
            Variable v = new Variable();
            v.id = token.getValue();
            token = input.nextToken();
            return v;
        }
        if (token.getType().equals("Literal")) {
            Value v = token.getValue().equals("true") ?
                      new Value(true) :
                      token.getValue().equals("false") ?
                      new Value(false) :
                      new Value(Integer.parseInt(token.getValue()));
            token = input.nextToken();
            return v;
        }
        if (token.getValue().equals("(")) {
            token = input.nextToken();
            Expression e = expression();
            match(")");
            return e;
        }
        throw new RuntimeException(SyntaxError("Identifier | Literal | ("));
    }

    private Conditional ifStatement() {
        Conditional c = new Conditional();
        match("if");
        match("(");
        c.test = expression();
        match(")");
        c.thenbranch = statement();
        if (token.getValue().equals("else")) {
            token = input.nextToken();
            c.elsebranch = statement();
        }
        return c;
    }

    private Loop whileStatement() {
        Loop l = new Loop();
        match("while");
        match("(");
        l.test = expression();
        match(")");
        l.body = statement();
        return l;
    }
}
