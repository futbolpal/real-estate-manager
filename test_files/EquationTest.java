package test_files;

import reman.client.app.finance.equations.Equation;
import reman.client.app.finance.equations.exceptions.MathException;
import reman.common.database.UserManager;

public class EquationTest {
	public static void main(String[] args) {
		try {
			System.out.println(UserManager.instance().login("Futbolpal", "Jonathan"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("EQUATION PARSING");
		Equation e = null;
		try {
			/*function/variable test*/
			e = new Equation("    (1)-(5+2)+(2-3)+sum(5,6)+1-    x+((1)    )   ");/*2*/
			System.out.println("Expression="+e);
			//e = new Equation("avg(5,5,5,5,5)+x*(2-1.5)");/*7*/
			//e = new Equation("avg(sum(1,2,3,4,5),20)*2");/*35*/
			//e = new Equation("avg(sum(1,2,3,4,5),20,25,35,40)/(2.000-1.5)");/*54*/
			//e = new Equation("avg(x,sum(2,5,3,2),x,55)-(6+(32-5))");/*-14.25*/
			/*..................*/
			/*order of operation/paren test start*/
			//e = new Equation("10*2/5");/*e.evaluate()==4*/
			//e = new Equation("10*(2/(5+4))");/*2.2222222222222223*/
			//e = new Equation("10*2/(5+4)");/*2.2222222222222223*/
			//e = new Equation("10*2/5+4");/*8*/
			/*...................*/
			/*math exception error test*/
			//e = new Equation("5/(4-x)");/*when x = 4*/
			//e = new Equation("15%((x+11)-15)");/*when x = 4*/
			/*....................*/
			System.out.println("EQUATION PARSED");
			e.setVariable("x", 4);
			String result = e.evaluate();
			System.out.println("result=" + result);
			System.out.println("Expression="+e);
		} catch (MathException e1) {
			System.err.println(e1);
			System.err.println(e);
		}
	}
}
