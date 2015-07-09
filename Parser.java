package assign1;

import java.util.HashMap;

// Referred the code shared by the Professor for a Non Terminals -Term,Expr,Factor

/* 		OBJECT-ORIENTED RECOGNIZER FOR SIMPLE EXPRESSIONS
 expr    -> term   (+ | -) expr | term
 term    -> factor (* | /) term | factor
 factor  -> number | real_number | '(' expr ')'     
 */

public class Parser {
	public static void main(String[] args) {
		System.out.println("Enter expression, end with semi-colon!\n");
		Lexer.lex();
		new Program();
		// new Expr();
		Code.output();
	}
}

class Program { // program -> decls stmts end

	Decls declaration;
	Stmts stmnts;
	public static int index_cnt = 0;
	public static int var_cnt = 0;
	public static int instr_cntr = 0;
	public static HashMap<String, Integer> hmVariable = new HashMap<String, Integer>();

	public Program() {// program -> decls stmts end

		if (Lexer.nextToken == Token.KEY_INT) {
			declaration = new Decls();
		}
		Lexer.lex(); // for navigating to the next statement
		if (Lexer.nextToken != Token.KEY_END)
			stmnts = new Stmts();
		Code.gen("return");
	}

	public static void addAddress(int ichoice, int address) {

		int index = 0;
		String nStrInstr = null;
		String s = null;
		for (int i = Program.index_cnt - 1; i >= 0; i--) {
			s = Code.code[i];
			if (ichoice == 1) {
				if (s.contains("PLC_HLDR1")) {
					nStrInstr = s.replace("PLC_HLDR1",
							String.valueOf(Program.instr_cntr));
					Code.code[Program.index_cnt - 1 - index] = nStrInstr;
					break;
				}
			} else {
				if (ichoice == 2) {
					if (s.contains("PLC_HLDR2")) {
						nStrInstr = s.replace("PLC_HLDR2",
								String.valueOf(Program.instr_cntr));
						Code.code[Program.index_cnt - 1 - index] = nStrInstr;
						break;
					}
				} else {
					if (s.contains("PLC_HLDR2")) {
						nStrInstr = s.replace("PLC_HLDR2",
								String.valueOf(address));
						Code.code[Program.index_cnt - 1 - index] = nStrInstr;
						break;

					}
				}
			}

			index++;
		}

	}

}

class Expr { // expr -> term (+ | -) expr | term

	Term t;
	Expr e;
	char op;

	public Expr() {
		t = new Term();
		if (Lexer.nextToken == Token.ADD_OP || Lexer.nextToken == Token.SUB_OP) {
			op = Lexer.nextChar;
			Lexer.lex(); // to point to the next element after operator
			e = new Expr();
			Code.gen(Code.opcode(op));
		}
	}
}

class Term { // term -> factor (* | /) term | factor
	Factor f;
	Term t;
	char op;

	public Term() {
		f = new Factor();
		if (Lexer.nextToken == Token.MULT_OP || Lexer.nextToken == Token.DIV_OP) {
			op = Lexer.nextChar;
			Lexer.lex(); // to point to the next element after operator
			t = new Term();
			Code.gen(Code.opcode(op));
		}
	}
}

class Factor { // factor -> number | '(' expr ')'
	Expr e;
	int i;
	char var;

	public Factor() { // factor -> int_lit | id | ‘(‘ expr ‘)’
		switch (Lexer.nextToken) {
		case Token.INT_LIT: // number
			i = Lexer.intValue;
			Lexer.lex();
			if (i < 6) {
				Program.instr_cntr++;
				Code.gen("iconst_" + i);

			} else if (i <= 127) {
				Program.instr_cntr += 2;
				Code.gen("bipush " + i);
				// bipush
			} else {
				Program.instr_cntr += 3;
				Code.gen("sipush " + i);
				// sipush
			}
			break;

		case Token.LEFT_PAREN: // '('
			Lexer.lex();
			e = new Expr();
			Lexer.lex(); // skip over ')'
			break;
		case Token.ID:
			var = Lexer.ident;
			Lexer.lex();
			Integer index = Program.hmVariable.get(String.valueOf(var));
			Program.instr_cntr += 1;
			Code.gen("iload_" + String.valueOf(index));
			// Lexer.lex();
		default:
			break;
		}
	}
}

class Code {
	static String[] code = new String[100];
	static int codeptr = 0;
	static int[] instr_addrs = new int[100];

	public static void gen(String s) {
		Program.index_cnt++;
		code[codeptr] = s;
		codeptr++;
		instr_addrs[codeptr] = Program.instr_cntr;

	}

	public static String opcode(char op) {
		switch (op) {
		case '+':
			Program.instr_cntr += 1;
			return "iadd";
		case '-':
			Program.instr_cntr += 1;
			return "isub";
		case '*':
			Program.instr_cntr += 1;
			return "imul";
		case '/':
			Program.instr_cntr += 1;
			return "idiv";
		default:
			return "";
		}
	}

	public static void output() {
		for (int i = 0; i < codeptr; i++)
			System.out.println(instr_addrs[i] + ": " + code[i]);
	}
}

class Decls { // decls -> int idlist ;

	IdList idList;

	public Decls() {

		idList = new IdList();

	}

}

class IdList { // idlist -> id { , id }

	public IdList() {

		while (Lexer.nextToken != Token.SEMICOLON) {
			Lexer.lex();
			if (Lexer.nextToken == 14) {
				Program.hmVariable.put(String.valueOf(Lexer.ident),
						Program.var_cnt);
				Program.var_cnt++;
			}
		}
	}

}

class Stmts { // stmts -> stmt [ stmts ]

	Stmt b_stmt;
	Stmts s_stmts;

	public Stmts() {

		if (Lexer.nextToken == Token.ID || Lexer.nextToken == Token.KEY_IF
				|| Lexer.nextToken == Token.KEY_WHILE) {
			b_stmt = new Stmt();

		}
		if ((Lexer.nextToken != Token.KEY_END && Lexer.nextToken != Token.KEY_ELSE)
				|| Lexer.nextToken == Token.ID
				|| Lexer.nextToken == Token.KEY_IF
				|| Lexer.nextToken == Token.KEY_WHILE) {
			Lexer.lex();
			s_stmts = new Stmts();
		}
		// Lexer.lex();
		// if(Lexer.nextToken != Token.KEY_END){
		// Code.gen("return");
		// }
	}

}

class Stmt { // stmt -> assign | loop | cond

	Assign assign;
	Loop loop;
	Cond cond;
	char var;
	int index;

	public Stmt() {

		// Lexer.lex();
		// Process the conditional statement
		if (Lexer.nextToken == Token.KEY_END) {
			return;
		}

		if (Lexer.nextToken == Token.KEY_IF) {

			cond = new Cond();

		} else {

			var = Lexer.ident;
			// Lexer.lex();
			// Lexer.lex();
			if (Lexer.nextToken == Token.ID) {
				Lexer.lex();
			}
			switch (Lexer.nextToken) {

			case Token.ASSIGN_OP: // number
				assign = new Assign();
				index = Program.hmVariable.get(String.valueOf(var));
				Program.instr_cntr += 1;
				Code.gen("istore_" + String.valueOf(index));
				// Code.gen("iconst_" + i);
				break;

			case Token.KEY_WHILE:
				loop = new Loop();
				break;
			default:
				break;

			}
		}
	}
}

class Assign { // assign -> id = expr ;

	Expr ex;
	char var;
	int index;

	public Assign() {

		Lexer.lex();
		// var = Lexer.ident;
		ex = new Expr();
		// index = Program.hmVariable.get(String.valueOf(var));
		// Code.gen("iStore_" + String.valueOf(index));
	}
}

class Cond { // cond -> if '(' rexp ')' cmpdstmt [ else cmpdstmt ]

	Rexpr r;
	Cmpdstmt cpstmt1, cpstmt2;
	Cond cnd;

	public Cond() {

		Lexer.lex();
		if (Lexer.nextToken == Token.KEY_IF
				|| Lexer.nextToken == Token.LEFT_PAREN) {

			Lexer.lex();
			r = new Rexpr();
			Lexer.lex(); // skip over ')'
			cpstmt1 = new Cmpdstmt();

			// call the method to replace the substring place holders with
			// address

			if (Lexer.nextToken != Token.KEY_ELSE) {
				Program.addAddress(1, 0);
			}
		}
		// Lexer.lex();
		if (Lexer.nextToken == Token.KEY_ELSE) {
			Program.instr_cntr += 3;
			Code.gen("goto PLC_HLDR2");
			Program.addAddress(1, 0);
			cpstmt2 = new Cmpdstmt();
		}
		Program.addAddress(2, 0);

	}

}

class Loop { // loop -> while '(' rexp ')' cmpdstmt

	Rexpr r_expr;
	Cmpdstmt c_cmpstmt;

	public Loop() {

		// Lexer.lex();

		// if(Lexer.nextToken == Token.KEY_WHILE){
		int prev_addr = Program.instr_cntr;
		Lexer.lex();
		Lexer.lex();

		r_expr = new Rexpr();
		// Lexer.lex(); //skip ')'
		c_cmpstmt = new Cmpdstmt();
		// }
		// if (Lexer.nextToken != Token.KEY_ELSE) {
		Program.instr_cntr += 3;
		Code.gen("goto PLC_HLDR2");
		Program.addAddress(1, 0);
		Program.addAddress(3, prev_addr);
		// }

	}

}

class Cmpdstmt { // cmpdstmt-> '{' stmts '}'

	Stmts stmts;

	public Cmpdstmt() {

		Lexer.lex(); // '{'
		stmts = new Stmts();
		// Lexer.lex(); // skip over the '}'

	}

}

class Rexpr { // rexp -> expr (< | > | = | !=) expr

	Expr e1, e2;
	StringBuilder sb = new StringBuilder(500);

	public Rexpr() {

		// Lexer.lex();
		e1 = new Expr();
		// Lexer.lex();
		if (Lexer.nextToken == Token.GREATER_OP) {
			sb.append(Constants.GTOPCODE).append(" ").append("PLC_HLDR1");

		} else if (Lexer.nextToken == Token.LESSER_OP) {
			sb.append(Constants.LTOPCODE).append(" ").append("PLC_HLDR1");
			// Program.instr_cntr += 3;
		} else {
			sb.append(Constants.EQOPCODE).append(" ").append("PLC_HLDR1");
			// Program.instr_cntr += 3;
		}
		Lexer.lex();
		e2 = new Expr();
		Program.instr_cntr += 3;
		Code.gen(sb.toString());

	}

}

class Constants {

	public static final String ICONST = "iconst_";
	public static final String GTOPCODE = "if_icmple";
	public static final String LTOPCODE = "if_icmpge";
	public static final String EQOPCODE = "if_icmpeq";

}
