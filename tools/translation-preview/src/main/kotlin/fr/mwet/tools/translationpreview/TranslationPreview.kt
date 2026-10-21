package fr.mwet.tools.translationpreview

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import fr.mwet.snake.Boot
import fr.mwet.snake.FontSwitcher
import fr.mwet.snake.GameContext
import fr.mwet.snake.LanguageSwitcher
import fr.mwet.snake.LoadedAssets
import fr.mwet.snake.Navigator
import fr.mwet.snake.assets.loadTranslations
import fr.mwet.snake.inputs.LibGdxKeyNames
import fr.mwet.snake.tuning.Palette
import fr.mwet.snake.ui.UiStyle
import ktx.scene2d.Scene2DSkin
import java.util.Locale
import fr.mwet.tools.translationpreview.PreviewMetrics.CURSOR_WIDTH
import fr.mwet.tools.translationpreview.PreviewMetrics.FIELD_WIDTH
import fr.mwet.tools.translationpreview.PreviewMetrics.GAP
import fr.mwet.tools.translationpreview.PreviewMetrics.PHONE_HEIGHT
import fr.mwet.tools.translationpreview.PreviewMetrics.PHONE_WIDTH

class TranslationPreview(private val edits: TranslationEdits) : ApplicationAdapter() {
    private lateinit var boot: Boot
    private lateinit var stage: Stage
    private lateinit var status: Label
    private lateinit var scenarios: List<Scenario>

    override fun create() {
        boot = Boot()
        val assets = loadAssets()
        loadTranslations(Locale.forLanguageTag(edits.language))
        val context = GameContext(boot, assets, LibGdxKeyNames, noOp<Navigator>(), noOp<LanguageSwitcher>(), noOp<FontSwitcher>())
        val screens = mapOf(
            "Main menu" to context.mainMenuScreen,
            "Game" to context.gameScreen,
            "Scores" to context.scoresScreen,
            "Settings" to context.settingsScreen,
            "Controls" to context.controlsScreen,
            "Language" to context.languageScreen,
            "Font" to context.fontScreen,
        )
        screens.values.forEach {
            it.show()
            it.resize(PHONE_WIDTH, PHONE_HEIGHT)
        }
        val matcher = TextMatcher(edits.keys.associateWith { edits[it] })
        scenarios = gameScenarios(boot.batch, PHONE_WIDTH, PHONE_HEIGHT)
        val occurrences = (screens.flatMap { (name, screen) -> findOccurrences(name, findStages(screen), matcher) } +
            scenarios.flatMap { findOccurrences(it.name, listOf(it.layer.stage), matcher) }).groupBy { it.key }

        val skin = Scene2DSkin.defaultSkin
        val pixel = assets.textures.rectangleTexture(1, 1, Color.WHITE)
        stage = Stage(ScreenViewport(), SpriteBatch())
        stage.addActor(layout(skin, pixel, occurrences))
        Gdx.input.inputProcessor = stage
    }

    private fun loadAssets(): LoadedAssets {
        while (true) {
            boot.assetLoading.update()?.let { return it }
        }
    }

    private fun layout(skin: Skin, pixel: Texture, occurrences: Map<String, List<Occurrence>>): Actor {
        val fieldStyle = textFieldStyle(skin, pixel)
        val rows = Table().top().left().pad(GAP)
        rows.defaults().pad(GAP / 2).top().left()
        edits.keys.forEach { key ->
            val shown = occurrences[key].orEmpty()
            val previews = Table().left()
            shown.forEach { occurrence ->
                previews.add(Label(occurrence.screen, skin, UiStyle.HIGHLIGHT)).left().row()
                previews.add(BlockPreview(occurrence, pixel)).left().row()
            }
            if (shown.isEmpty()) previews.add(Label("Not shown on a screen", skin)).left()
            val field = TextField(edits[key], fieldStyle)
            field.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    edits[key] = field.text
                    shown.forEach { it.show(display(field.text, it.arguments)) }
                    previews.invalidateHierarchy()
                    status.setText("Not saved")
                }
            })
            rows.add(Label(key, skin))
            rows.add(field).width(FIELD_WIDTH)
            rows.add(previews).expandX().row()
        }

        status = Label("${edits.language}: ${occurrences.size} of ${edits.keys.size} keys shown", skin)
        val save = TextButton("Save", skin)
        save.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                edits.save()
                status.setText("Saved. Rebuild to see it in the game.")
            }
        })
        val scroll = ScrollPane(rows).apply { setFadeScrollBars(false) }
        return Table().apply {
            setFillParent(true)
            top().left().pad(GAP)
            add(save).left()
            add(status).left().padLeft(GAP).expandX().row()
            add(scroll).colspan(2).grow()
            stage.scrollFocus = scroll
        }
    }

    private fun textFieldStyle(skin: Skin, pixel: Texture) = TextField.TextFieldStyle().apply {
        font = skin.get(Label.LabelStyle::class.java).font
        fontColor = Palette.label
        background = skin.getDrawable(UiStyle.PANEL)
        cursor = TextureRegionDrawable(TextureRegion(pixel)).tint(Palette.label).apply { minWidth = CURSOR_WIDTH }
        selection = TextureRegionDrawable(TextureRegion(pixel)).tint(Palette.highlight)
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun render() {
        ScreenUtils.clear(Palette.screenClear)
        stage.act()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
        scenarios.forEach { it.layer.dispose() }
        boot.dispose()
    }
}
