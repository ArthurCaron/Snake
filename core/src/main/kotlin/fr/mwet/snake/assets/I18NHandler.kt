package fr.mwet.snake.assets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.I18NBundle
import fr.mwet.snake.generated.assets.I18N
import java.util.Locale

private const val I18N_ROOT = "i18n/translations"

fun loadTranslations(locale: Locale) {
    I18N.i18nBundle = I18NBundle.createBundle(Gdx.files.internal(I18N_ROOT), locale)
}
