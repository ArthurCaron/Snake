package fr.mwet.snake.screens.options

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import fr.mwet.snake.Navigator
import fr.mwet.snake.backdrop.BackdropStyle
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.render.Appearance
import fr.mwet.snake.render.GroundStyle
import fr.mwet.snake.render.VisualTheme
import fr.mwet.snake.ui.SelectionBadge
import fr.mwet.snake.render.MarkShape
import fr.mwet.snake.screens.appearance.AppearancePreview
import fr.mwet.snake.transition.TransitionStyle
import fr.mwet.snake.ui.MenuPage
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.UiMetrics
import fr.mwet.snake.ui.widthForCharacters
import ktx.app.KtxScreen
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textField

enum class OptionPage { Theme, Transition, Backdrop, Ground, Symbol, BodyColor, MarksColor }

class OptionsScreen(private val kit: UiKit, private val navigator: Navigator) : KtxScreen {
    private val settings = kit.settingsRepository
    private var category = OptionPage.Transition
    private val page = MenuPage(kit, "", onBack = ::back)
    private val buttons = mutableListOf<Pair<TextButton, () -> String>>()
    private var colorField: TextField? = null

    fun configure(category: OptionPage) {
        this.category = category
        buttons.clear()
        colorField = null
        page.content.clearChildren()
        page.header.clearChildren()
        page.title = when (category) {
            OptionPage.Theme -> I18N.visualTheme()
            OptionPage.Transition -> I18N.transitionTitle()
            OptionPage.Backdrop -> I18N.backdropTitle()
            OptionPage.Ground -> I18N.groundTitle()
            OptionPage.Symbol -> I18N.snakeSymbol()
            OptionPage.BodyColor -> I18N.snakeBodyColor()
            OptionPage.MarksColor -> I18N.snakeMarksColor()
        }
        if (category in listOf(OptionPage.Theme, OptionPage.Symbol, OptionPage.BodyColor, OptionPage.MarksColor)) {
            page.header.actor(AppearancePreview(kit)) { it.fillX().row() }
        }
        when (category) {
            OptionPage.Theme -> {
                page.content.label(I18N.themeHint()) { setFontScale(0.7f); it.row() }
                VisualTheme.entries.forEach { theme ->
                    val badge = SelectionBadge(kit.textures).apply { showColor(theme.defaultAppearance().body) }
                    choice(themeName(theme), { settings.appearance.theme == theme }, badge) { navigator.applyVisualTheme(theme) }
                }
            }
            OptionPage.Transition -> TransitionStyle.entries.forEach { style ->
                choice(transitionName(style), { settings.transition == style }) {
                    settings.setTransition(style)
                    navigator.previewTransition()
                }
            }
            OptionPage.Backdrop -> BackdropStyle.entries.forEach { style ->
                choice(backdropName(style), { settings.backdrop == style }) { settings.setBackdrop(style) }
            }
            OptionPage.Ground -> GroundStyle.entries.forEach { style ->
                choice(groundName(style), { settings.ground == style }) { settings.setGround(style) }
            }
            OptionPage.Symbol -> MarkShape.entries.forEach { shape ->
                val image = SelectionBadge(kit.textures).apply { showSymbol(shape.region(kit.textures.skin), settings.appearance.marks) }
                choice(symbolName(shape), { settings.appearance.markShape == shape }, image) {
                    settings.setAppearance(settings.appearance.copy(markShape = shape))
                }
            }
            OptionPage.BodyColor, OptionPage.MarksColor -> colorChoices()
        }
        refresh()
    }

    private fun choice(name: String, selected: () -> Boolean, image: SelectionBadge? = null, apply: () -> Unit) {
        val button = kit.widgets.menuButton(name, onClick = { apply(); refresh() })
        if (image != null) {
            button.labelCell.expandX()
            button.add(image).size(UiMetrics.CHOICE_BADGE_SIZE).padLeft(UiMetrics.PAGE_CONTENT_SPACE)
        }
        page.content.add(button).fillX().row()
        buttons += button to { if (selected()) "> $name <" else name }
    }

    private fun colorChoices() {
        val presets = listOf(
            I18N.colorTurquoise() to Appearance.DEFAULT_BODY,
            I18N.colorCream() to Appearance.DEFAULT_MARKS,
            I18N.colorGreen() to "79bd54",
            I18N.colorBlue() to "549fdb",
            I18N.colorPurple() to "ad72ce",
            I18N.colorPink() to "e67d9f",
            I18N.colorOrange() to "ed994b",
            I18N.colorRed() to "dc5b53",
        )
        presets.forEach { (name, hex) ->
            choice(name, { currentHex() == hex }, SelectionBadge(kit.textures).apply { showColor(Color.valueOf(hex)) }) { setColor(hex) }
        }
        with(kit.widgets) {
            page.content.label(I18N.customColor()) { it.row() }
            page.content.table {
                val field: TextField = textField(currentHex()) {
                    maxLength = 7
                    textFieldFilter = TextField.TextFieldFilter { _, c -> c == '#' || c in '0'..'9' || c.lowercaseChar() in 'a'..'f' }
                    it.width(maxOf(UiMetrics.COLOR_FIELD_WIDTH, widthForCharacters("#0123456789abcdefABCDEF", maxLength)))
                }
                colorField = field
                menuButton(I18N.applyColor(), onClick = {
                    val hex = Appearance.colorHex(field.text)
                    if (hex != null) {
                        setColor(hex)
                        field.color = Color.WHITE
                        refresh()
                    } else field.color = Color.SALMON
                }) { it.padLeft(UiMetrics.PAGE_CONTENT_SPACE) }
                it.row()
            }
            page.content.label(I18N.colorFormat()) { setFontScale(0.7f); it.row() }
        }
    }

    private fun currentHex() = if (category == OptionPage.BodyColor) settings.appearance.bodyHex else settings.appearance.marksHex

    private fun setColor(hex: String) {
        val look = settings.appearance
        settings.setAppearance(if (category == OptionPage.BodyColor) look.copy(bodyHex = hex) else look.copy(marksHex = hex))
    }

    private fun refresh() {
        buttons.forEach { (button, label) -> button.setText(label()) }
        colorField?.let { it.text = currentHex(); it.color = Color.WHITE }
    }

    private fun back() {
        if (category in listOf(OptionPage.Transition, OptionPage.Backdrop, OptionPage.Ground)) navigator.goToDevOptions()
        else navigator.goToAppearance()
    }

    override fun show() { page.show(); refresh(); page.scrollToTop() }
    override fun hide() = page.hide()
    override fun resize(width: Int, height: Int) = page.resize(width, height)
    override fun render(delta: Float) = page.render(delta)
    override fun dispose() = page.dispose()
}

internal fun transitionName(style: TransitionStyle) = when (style) {
    TransitionStyle.None -> I18N.transitionNone()
    TransitionStyle.Fade -> I18N.transitionFade()
    TransitionStyle.Slide -> I18N.transitionSlide()
    TransitionStyle.SnakeWipe -> I18N.transitionSnake()
}

internal fun backdropName(style: BackdropStyle) = when (style) {
    BackdropStyle.Plain -> I18N.backdropPlain()
    BackdropStyle.DriftingGrid -> I18N.backdropGrid()
    BackdropStyle.DemoSnake -> I18N.backdropDemo()
    BackdropStyle.FloatingFruits -> I18N.backdropFruits()
    BackdropStyle.Fireflies -> I18N.backdropFireflies()
    BackdropStyle.DiagonalStripes -> I18N.backdropStripes()
    BackdropStyle.Waves -> I18N.backdropWaves()
    BackdropStyle.Ripples -> I18N.backdropRipples()
    BackdropStyle.GameOfLife -> I18N.backdropLife()
    BackdropStyle.WanderingSnakes -> I18N.backdropWanderers()
}

internal fun groundName(style: GroundStyle) = when (style) {
    GroundStyle.Checker -> I18N.groundChecker()
    GroundStyle.Plain -> I18N.groundPlain()
    GroundStyle.Grain -> I18N.groundGrain()
    GroundStyle.Stripes -> I18N.groundStripes()
    GroundStyle.Tiles -> I18N.groundTiles()
    GroundStyle.Scales -> I18N.groundScales()
    GroundStyle.Dunes -> I18N.groundDunes()
    GroundStyle.Hex -> I18N.groundHex()
}

internal fun symbolName(shape: MarkShape) = when (shape) {
    MarkShape.None -> I18N.symbolNone()
    MarkShape.Diamond -> I18N.symbolDiamond()
    MarkShape.Circle -> I18N.symbolCircle()
    MarkShape.Square -> I18N.symbolSquare()
    MarkShape.Star -> I18N.symbolStar()
    MarkShape.Chevron -> I18N.symbolChevron()
    MarkShape.Heart -> I18N.symbolHeart()
}

internal fun themeName(theme: VisualTheme) = when (theme) {
    VisualTheme.Desert -> I18N.themeDesert()
    VisualTheme.Laser -> I18N.themeLaser()
}
