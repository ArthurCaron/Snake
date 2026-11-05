package fr.mwet.snake.screens.devoptions

import fr.mwet.snake.Navigator
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.UiKit
import ktx.app.KtxScreen

class DevOptionsScreen(kit: UiKit, navigator: Navigator) : KtxScreen {
    private val page = MenuPage(kit, I18N.devOptions(), onBack = navigator::goToMainMenu)

    init {
        with(kit.widgets) {
            page.content.menuButton(I18N.appearance(), onClick = navigator::goToAdminAppearance) { it.fillX().row() }
        }
    }

    override fun show() = page.show()
    override fun hide() = page.hide()
    override fun resize(width: Int, height: Int) = page.resize(width, height)
    override fun render(delta: Float) = page.render(delta)
    override fun dispose() = page.dispose()
}
