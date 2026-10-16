package fr.mwet.snake.screens.scores

import fr.mwet.snake.Navigator
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.save.metadata.MetadataRepository
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.UiKit
import ktx.app.KtxScreen
import ktx.scene2d.label

class ScoresScreen(kit: UiKit, private val metadataRepository: MetadataRepository, navigator: Navigator) : KtxScreen {
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
        page.content.apply {
            clear()
            val highScores = metadataRepository.metadata.highScores
            if (highScores.isEmpty()) {
                label(I18N.noScores())
                return
            }
            highScores.forEachIndexed { index, highScore ->
                label("${index + 1}.") { it.right().padRight(32f) }
                label(highScore.score.toString()) { it.right().row() }
            }
        }
    }
}
