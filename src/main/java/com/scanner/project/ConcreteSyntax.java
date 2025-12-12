package com.scanner.project;
// ConcreteSyntax.java

public class ConcreteSyntax {

	public Token token;
	public TokenStream input; 

	public ConcreteSyntax(TokenStream ts) { 
		input = ts; 
		token = input.nextToken(); 
	}

	private String SyntaxError(String tok) {
		String s = "Syntax error - Expecting: " + tok + " But saw: "
				+ token.getType() + " = " + token.getValue();
		System.out.println(s);
		return s;
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
		Declarations ds = new Declarations();
		while (token.getValue().equals("integer")
				|| token.getValue().equals("bool")) {
			declaration(ds);
		}
		return ds;
	}

	private void declaration(Declarations ds) {
		Type t = type();
		identifiers(ds, t);
		match(";");
	}

	private Type type() {
		Type t;
		if (token.getValue().equals("integer"))
			t = new Type("integer");
		else if (token.getValue().equals("bool"))
			t = new Type("bool");
		else
			throw new RuntimeException(SyntaxError("integer | bool"));
		token = input.nextToken(); 
		return t;
	}

	private void identifiers(Declarations ds, Type t) {
		Declaration d = new Declaration(); 
		d.t = t; 
		if (token.getType().equals("Identifier")) {
			d.v = new Variable();
			d.v.id = token.getValue(); 
			ds.addElement(d);
			token = input.nextToken();
			while (token.getValue().equals(",")) {
				d = new Declaration(); 
				d.t = t; 
				token = input.nextToken();
				if (token.getType().equals("Identifier")) {
					d.v = new Variable(); 
					d.v.id = token.getValue();
					ds.addElement(d);
					token = input.nextToken(); 
				} else
					throw new RuntimeException(SyntaxError("Identifier"));
			}
		} else
			throw new RuntimeException(SyntaxError("Identifier"));
	}

	private Statement statement() {
		Statement s = new Skip();
		if (token.getValue().equals(";")) { 
			token = input.nextToken();
			return s;
		} else if (token.getValue().equals("{")) { 
			token = input.nextToken();
			s = statements();
			match("}");
		} else if (token.getValue().equals("if")) {
			s = ifStatement();
		} else if (token.getValue().equals("while")) {
			s = whileStatement();
		} else if (token.getType().equals("Identifier")) { 
			s = assignment();
		} else {
			throw new RuntimeException(SyntaxError("Statement"));
		}
		return s;
	}

	private Block statements() {
		Block b = new Block();
		while (!token.getValue().equals("}")) {
			b.blockmembers.addElement(statement());
		}
		return b;
	}

	private Assignment assignment() {
		Assignment a = new Assignment();
		if (token.getType().equals("Identifier")) {
			a.target = new Variable();
			a.target.id = token.getValue();

			token = input.nextToken();
			match(":=");
			a.source = expression();
			match(";");
		} else {
			throw new RuntimeException(SyntaxError("Identifier"));
		}
		return a;
	}

	private Expression expression() {
		Binary b;
		Expression e = conjunction();
		while (token.getValue().equals("||")) {
			b = new Binary();
			b.term1 = e;
			b.op = new Operator(token.getValue());
			token = input.nextToken();
			b.term2 = conjunction();
			e = b;
		}
		return e;
	}

	private Expression conjunction() {
		Binary b;
		Expression e = relation();
		while (token.getValue().equals("&&")) {
			b = new Binary();
			b.term1 = e;
			b.op = new Operator(token.getValue());
			token = input.nextToken();
			b.term2 = relation();
			e = b;
		}
		return e;
	}

	private Expression relation() {
		Binary b;
		Expression e = addition();
		while (token.getValue().equals("<") ||
           token.getValue().equals("<=") ||
           token.getValue().equals(">") ||
           token.getValue().equals(">=") ||
           token.getValue().equals("==") ||
           token.getValue().equals("!=") || 
           token.getValue().equals("<>")) {
			b = new Binary();
			b.term1 = e;
			b.op = new Operator(token.getValue());
			token = input.nextToken();
			b.term2 = addition();
			e = b;
		}
		return e;
	}

	private Expression addition() {
		Binary b;
		Expression e = term();
		while (token.getValue().equals("+") || token.getValue().equals("-")) {
			b = new Binary();
			b.term1 = e;
			b.op = new Operator(token.getValue());
			token = input.nextToken();
			b.term2 = term();
			e = b;
		}
		return e;
	}

	private Expression term() {
		Binary b;
		Expression e = negation();
		while (token.getValue().equals("*") || token.getValue().equals("/")) {
			b = new Binary();
			b.term1 = e;
			b.op = new Operator(token.getValue());
			token = input.nextToken();
			b.term2 = negation();
			e = b;
		}
		return e;
	}

	private Expression negation() {
		Unary u;
		if (token.getValue().equals("!")) {
			u = new Unary();
			u.op = new Operator(token.getValue());
			token = input.nextToken();
			u.term = negation(); 
			return u;
		} return factor();
	}

	private Expression factor() {
		Expression e = null;
		if (token.getType().equals("Identifier")) {
			Variable v = new Variable();
			v.id = token.getValue();
			e = v;
			token = input.nextToken();
		} else if (token.getType().equals("Literal")) {
			Value v = null;
			if (isInteger(token.getValue()))
				v = new Value((new Integer(token.getValue())).intValue());
			else if (token.getValue().equals("true"))
				v = new Value(true);
			else if (token.getValue().equals("false"))
				v = new Value(false);
			else
				throw new RuntimeException(SyntaxError("Literal"));
			e = v;
			token = input.nextToken();
		} else if (token.getValue().equals("(")) {
			token = input.nextToken();
			e = expression();
			match(")");
		} else {
			throw new RuntimeException(SyntaxError("Identifier | Literal | ("));
		}
		return e;
	}

	private Conditional ifStatement() {
		Conditional c = new Conditional();
		match("if");
		match("(");
		c.test = expression();
		match(")");
		c.thenbranch = statement();
		if (token.getValue().equals("else")) {
			match("else");
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

	private boolean isInteger(String s) {
		boolean result = true;
		for (int i = 0; i < s.length(); i++)
			if ('0' > s.charAt(i) || '9' < s.charAt(i))
				result = false;
		return result;
	}
}
