package io.stephub.server.service;


import io.stephub.server.api.model.Workspace;
import io.stephub.server.api.model.gherkin.Feature;
import io.stephub.server.model.features.generated.FeaturesLexer;
import io.stephub.server.model.features.generated.FeaturesParser;
import io.stephub.server.model.features.grammar.FeatureVisitor;
import io.stephub.server.model.features.grammar.SyntaxErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.springframework.stereotype.Service;

@Service
public class FeatureParser {

    public Feature parseFeature(final Workspace workspace, final String featureFile) {
        final FeaturesLexer lexer = new FeaturesLexer(CharStreams.fromString(featureFile));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new SyntaxErrorListener(featureFile));
        final TokenStream tokens = new CommonTokenStream(lexer);
        final FeaturesParser parser = new FeaturesParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new SyntaxErrorListener(featureFile));
        final FeatureVisitor visitor = new FeatureVisitor();
        final Feature result = visitor.visit(parser.feature());
        return result;
    }
}
