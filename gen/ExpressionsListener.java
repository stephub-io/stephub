// Generated from /home/mbok/IdeaProjects/cucumberform/src/main/antlr4/Expressions.g4 by ANTLR 4.8
package org.mbok.cucumberform.expression.generated;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ExpressionsParser}.
 */
public interface ExpressionsListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ExpressionsParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(ExpressionsParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionsParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(ExpressionsParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionsParser#json}.
	 * @param ctx the parse tree
	 */
	void enterJson(ExpressionsParser.JsonContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionsParser#json}.
	 * @param ctx the parse tree
	 */
	void exitJson(ExpressionsParser.JsonContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionsParser#obj}.
	 * @param ctx the parse tree
	 */
	void enterObj(ExpressionsParser.ObjContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionsParser#obj}.
	 * @param ctx the parse tree
	 */
	void exitObj(ExpressionsParser.ObjContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionsParser#pair}.
	 * @param ctx the parse tree
	 */
	void enterPair(ExpressionsParser.PairContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionsParser#pair}.
	 * @param ctx the parse tree
	 */
	void exitPair(ExpressionsParser.PairContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionsParser#arr}.
	 * @param ctx the parse tree
	 */
	void enterArr(ExpressionsParser.ArrContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionsParser#arr}.
	 * @param ctx the parse tree
	 */
	void exitArr(ExpressionsParser.ArrContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionsParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(ExpressionsParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionsParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(ExpressionsParser.ValueContext ctx);
}