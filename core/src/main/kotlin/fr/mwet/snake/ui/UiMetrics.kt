package fr.mwet.snake.ui

object UiMetrics {
    const val REFERENCE_BOARD_WIDTH = 900f
    const val UNIT = 4f

    // The 7 px bevel at the bottom of a button image lifts its face 3.5 px above the middle of the button. Each button
    // pads its content that much higher, so that the content sits in the middle of the face. Pressed, the face moves
    // down by BUTTON_PRESS_DEPTH, and pressedOffsetY moves the content down with it.
    const val BUTTON_FACE_LIFT = UNIT * 0.875f
    const val BUTTON_PRESS_DEPTH = UNIT
    // Centred capitals still look low in mixed-case text: the lowercase letters and the descenders sit below the
    // middle. Text buttons lift their label this much more than icon buttons.
    const val TEXT_LIFT = UNIT

    const val BUTTON_PAD_TOP = UNIT * 4 - BUTTON_FACE_LIFT - TEXT_LIFT
    const val BUTTON_PAD_BOTTOM = UNIT * 4 + BUTTON_FACE_LIFT + TEXT_LIFT
    const val BUTTON_PAD_SIDE = UNIT * 6
    const val PLAY_BUTTON_PAD_TOP = UNIT * 6.25f - BUTTON_FACE_LIFT - TEXT_LIFT
    const val PLAY_BUTTON_PAD_BOTTOM = UNIT * 6.25f + BUTTON_FACE_LIFT + TEXT_LIFT
    const val PLAY_BUTTON_PAD_SIDE = UNIT * 18
    const val ICON_SIZE = UNIT * 11
    const val ICON_BUTTON_PAD_TOP = UNIT * 3.75f - BUTTON_FACE_LIFT
    const val ICON_BUTTON_PAD_BOTTOM = UNIT * 3.75f + BUTTON_FACE_LIFT
    const val ICON_BUTTON_PAD_SIDE = UNIT * 3.5f
    const val FLAG_WIDTH = ICON_SIZE * 3 / 2

    const val SCREEN_MARGIN = UNIT * 6
    const val PANEL_PAD = UNIT * 8
    const val PAGE_SPACE = UNIT * 6
    const val PAGE_CONTENT_SPACE = UNIT * 3
    const val OVERLAY_SPACE = UNIT * 4
    const val HUD_PAD = UNIT * 2
    const val HUD_ICON_SIZE = UNIT * 7
    const val HUD_BUTTON_PAD_TOP = UNIT * 2 - BUTTON_FACE_LIFT
    const val HUD_BUTTON_PAD_BOTTOM = UNIT * 2 + BUTTON_FACE_LIFT
    const val HUD_BUTTON_PAD_SIDE = UNIT * 2
    const val BOARD_BOTTOM_BAND = UNIT * 4

    const val MENU_TITLE_WIDTH_RATIO = 0.7f
    const val MENU_TITLE_GAP = UNIT * 16
    const val MENU_BEST_SCORE_GAP = UNIT * 12
    const val READY_TITLE_GAP = UNIT * 6
    const val READY_TEXT_SIDE_PAD = UNIT * 8

    const val APPEARANCE_PREVIEW_WIDTH = UNIT * 110
    const val APPEARANCE_PREVIEW_HEIGHT = UNIT * 40
    const val CHOICE_SWATCH_SIZE = UNIT * 9
    const val COLOR_FIELD_WIDTH = UNIT * 48

    const val CONTROLS_LABEL_GAP = UNIT * 3
    const val CONTROLS_RESET_GAP = UNIT * 3
    const val KEY_COLUMN_GAP = UNIT * 4
}
