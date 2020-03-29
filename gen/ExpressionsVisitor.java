// Generated from /home/mbok/IdeaProjects/cucumberform/src/main/antlr4/Expressions.g4 by ANTLR 4.8
package org.mbok.cucumberform.expression.generated;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ExpressionsParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ExpressionsVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ExpressionsParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(ExpressionsParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionsParser#json}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson(ExpressionsParser.JsonContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionsParser#obj}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObj(ExpressionsParser.ObjContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionsParser#pair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPair(ExpressionsParser.PairContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionsParser#arr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArr(ExpressionsParser.ArrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionsParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(ExpressionsParser.ValueContext ctx);
}