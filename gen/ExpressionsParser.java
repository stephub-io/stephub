// Generated from /home/mbok/IdeaProjects/cucumberform/src/main/antlr4/Expressions.g4 by ANTLR 4.8
package org.mbok.cucumberform.expression.generated;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ExpressionsParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		STRING=10, NUMBER=11, WS=12;
	public static final int
		RULE_expr = 0, RULE_json = 1, RULE_obj = 2, RULE_pair = 3, RULE_arr = 4, 
		RULE_value = 5;
	private static String[] makeRuleNames() {
		return new String[] {
			"expr", "json", "obj", "pair", "arr", "value"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "','", "'}'", "':'", "'['", "']'", "'true'", "'false'", 
			"'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, "STRING", 
			"NUMBER", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Expressions.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


	  // For collecting info whilst processing rules that can be used in messages
	  // protected Stack<String> paraphrase = new Stack<String>();

	public ExpressionsParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class ExprContext extends ParserRuleContext {
		public JsonContext json() {
			return getRuleContext(JsonContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ExpressionsParser.EOF, 0); }
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionsListener ) ((ExpressionsListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionsListener ) ((ExpressionsListener)listener).exitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionsVisitor ) return ((ExpressionsVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(12);
			json();
			setState(13);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JsonContext extends ParserRuleContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public JsonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_json; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionsListener ) ((ExpressionsListener)listener).enterJson(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionsListener ) ((ExpressionsListener)listener).exitJson(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionsVisitor ) return ((ExpressionsVisitor<? extends T>)visitor).visitJson(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JsonContext json() throws RecognitionException {
		JsonContext _localctx = new JsonContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_json);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(15);
			value();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ObjContext extends ParserRuleContext {
		public List<PairContext> pair() {
			return getRuleContexts(PairContext.class);
		}
		public PairContext pair(int i) {
			return getRuleContext(PairContext.class,i);
		}
		public ObjContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_obj; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionsListener ) ((ExpressionsListener)listener).enterObj(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionsListener ) ((ExpressionsListener)listener).exitObj(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionsVisitor ) return ((ExpressionsVisitor<? extends T>)visitor).visitObj(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjContext obj() throws RecognitionException {
		ObjContext _localctx = new ObjContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_obj);
		int _la;
		try {
			setState(30);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(17);
				match(T__0);
				setState(18);
				pair();
				setState(23);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(19);
					match(T__1);
					setState(20);
					pair();
					}
					}
					setState(25);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(26);
				match(T__2);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(28);
				match(T__0);
				setState(29);
				match(T__2);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PairContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(ExpressionsParser.STRING, 0); }
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public PairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionsListener ) ((ExpressionsListener)listener).enterPair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionsListener ) ((ExpressionsListener)listener).exitPair(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionsVisitor ) return ((ExpressionsVisitor<? extends T>)visitor).visitPair(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PairContext pair() throws RecognitionException {
		PairContext _localctx = new PairContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_pair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(32);
			match(STRING);
			setState(33);
			match(T__3);
			setState(34);
			value();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrContext extends ParserRuleContext {
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public ArrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionsListener ) ((ExpressionsListener)listener).enterArr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionsListener ) ((ExpressionsListener)listener).exitArr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionsVisitor ) return ((ExpressionsVisitor<? extends T>)visitor).visitArr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrContext arr() throws RecognitionException {
		ArrContext _localctx = new ArrContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_arr);
		int _la;
		try {
			setState(49);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(36);
				match(T__4);
				setState(37);
				value();
				setState(42);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__1) {
					{
					{
					setState(38);
					match(T__1);
					setState(39);
					value();
					}
					}
					setState(44);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(45);
				match(T__5);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(47);
				match(T__4);
				setState(48);
				match(T__5);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(ExpressionsParser.STRING, 0); }
		public TerminalNode NUMBER() { return getToken(ExpressionsParser.NUMBER, 0); }
		public ObjContext obj() {
			return getRuleContext(ObjContext.class,0);
		}
		public ArrContext arr() {
			return getRuleContext(ArrContext.class,0);
		}
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionsListener ) ((ExpressionsListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionsListener ) ((ExpressionsListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionsVisitor ) return ((ExpressionsVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_value);
		try {
			setState(58);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(51);
				match(STRING);
				}
				break;
			case NUMBER:
				enterOuterAlt(_localctx, 2);
				{
				setState(52);
				match(NUMBER);
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 3);
				{
				setState(53);
				obj();
				}
				break;
			case T__4:
				enterOuterAlt(_localctx, 4);
				{
				setState(54);
				arr();
				}
				break;
			case T__6:
				enterOuterAlt(_localctx, 5);
				{
				setState(55);
				match(T__6);
				}
				break;
			case T__7:
				enterOuterAlt(_localctx, 6);
				{
				setState(56);
				match(T__7);
				}
				break;
			case T__8:
				enterOuterAlt(_localctx, 7);
				{
				setState(57);
				match(T__8);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\16?\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\3\2\3\2\3\3\3\3\3\4\3\4\3\4\3\4"+
		"\7\4\30\n\4\f\4\16\4\33\13\4\3\4\3\4\3\4\3\4\5\4!\n\4\3\5\3\5\3\5\3\5"+
		"\3\6\3\6\3\6\3\6\7\6+\n\6\f\6\16\6.\13\6\3\6\3\6\3\6\3\6\5\6\64\n\6\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7=\n\7\3\7\2\2\b\2\4\6\b\n\f\2\2\2B\2\16"+
		"\3\2\2\2\4\21\3\2\2\2\6 \3\2\2\2\b\"\3\2\2\2\n\63\3\2\2\2\f<\3\2\2\2\16"+
		"\17\5\4\3\2\17\20\7\2\2\3\20\3\3\2\2\2\21\22\5\f\7\2\22\5\3\2\2\2\23\24"+
		"\7\3\2\2\24\31\5\b\5\2\25\26\7\4\2\2\26\30\5\b\5\2\27\25\3\2\2\2\30\33"+
		"\3\2\2\2\31\27\3\2\2\2\31\32\3\2\2\2\32\34\3\2\2\2\33\31\3\2\2\2\34\35"+
		"\7\5\2\2\35!\3\2\2\2\36\37\7\3\2\2\37!\7\5\2\2 \23\3\2\2\2 \36\3\2\2\2"+
		"!\7\3\2\2\2\"#\7\f\2\2#$\7\6\2\2$%\5\f\7\2%\t\3\2\2\2&\'\7\7\2\2\',\5"+
		"\f\7\2()\7\4\2\2)+\5\f\7\2*(\3\2\2\2+.\3\2\2\2,*\3\2\2\2,-\3\2\2\2-/\3"+
		"\2\2\2.,\3\2\2\2/\60\7\b\2\2\60\64\3\2\2\2\61\62\7\7\2\2\62\64\7\b\2\2"+
		"\63&\3\2\2\2\63\61\3\2\2\2\64\13\3\2\2\2\65=\7\f\2\2\66=\7\r\2\2\67=\5"+
		"\6\4\28=\5\n\6\29=\7\t\2\2:=\7\n\2\2;=\7\13\2\2<\65\3\2\2\2<\66\3\2\2"+
		"\2<\67\3\2\2\2<8\3\2\2\2<9\3\2\2\2<:\3\2\2\2<;\3\2\2\2=\r\3\2\2\2\7\31"+
		" ,\63<";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}