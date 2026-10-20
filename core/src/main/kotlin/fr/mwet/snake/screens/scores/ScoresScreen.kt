package fr.mwet.snake.screens.scores

import com.badlogic.gdx.scenes.scene2d.ui.Label
import fr.mwet.snake.Navigator
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.save.metadata.MetadataRepository
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.UiKit
import ktx.app.KtxScreen

class ScoresScreen(private val kit: UiKit, private val metadataRepository: MetadataRepository, navigator: Navigator) : KtxScreen {
    private val page = MenuPage(kit, I18N.highscore(), onBack = navigator::goToMainMenu)

    override fun show() {
        page.show()
        fillTable()
    }

    override fun hide() = page.hide()

    override fun resize(width: Int, height: Int) = page.resize(width, height)

    override fun render(delta: Float) = page.render(delta)

    override fun dispose() = page.dispose()

    private fun fillTable() {
        val table = page.content
        table.clear()
        val highScores = metadataRepository.metadata.highScores
        if (highScores.isEmpty()) {
            table.add(Label(I18N.noScores(), kit.styles.label))
            return
        }
        highScores.forEachIndexed { index, highScore ->
            table.add(Label("${index + 1}.", kit.styles.label)).right().padRight(32f)
            table.add(Label(highScore.score.toString(), kit.styles.label)).right()
            table.row()
        }
    }
}
