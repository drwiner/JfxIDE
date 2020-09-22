package sample.draganddrop;

import javafx.scene.layout.Pane;
import sample.language.syntax.SyntaxManager;

public class CodeArea extends Pane {

    public CodeArea() {
        this.getChildren().add(new CodeLineVisual(SyntaxManager.EFFECT_FACTORIES.get(12)));
        this.getChildren().add(new CodeLineVisual(SyntaxManager.EFFECT_FACTORIES.get(10)));
    }

}
