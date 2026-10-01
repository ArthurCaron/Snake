package fr.mwet.snake.screens

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import fr.mwet.snake.assets.I18N
import fr.mwet.snake.ui.GameUiStyles

class GameHud(styles: GameUiStyles) {
    private val scoreLabel = Label("", styles.label).apply { touchable = Touchable.disabled }
    private val bestLabel = Label("", styles.label).apply { touchable = Touchable.disabled }
    private var shownScore = -1
    private var shownBest = -1

    val actor = Table().apply {
        setFillParent(true)
        top().pad(16f)
        add(scoreLabel).expandX().left()
        add(bestLabel).expandX().right()
    }

    fun show(score: Int, best: Int) {
        if (score != shownScore) {
            shownScore = score
            scoreLabel.setText(I18N.score(score))
        }
        if (best != shownBest) {
            shownBest = best
            bestLabel.setText(I18N.best(best))
        }
    }
}
