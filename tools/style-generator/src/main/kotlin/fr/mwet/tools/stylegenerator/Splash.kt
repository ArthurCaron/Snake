package fr.mwet.tools.stylegenerator

// The "Loading..." text alone, 92 px, centred on a 1500 by 120 px canvas.
fun loadingText(p: Palette) = Raster(1500, 120).apply {
    canvas.outlinedText("Loading...", 750.0, 92.0, 92.0, p, align = Align.CENTER)
}

// The 640 by 400 px splash screen: the ground tiled at 75% scale, the board frame shadow stretched over the whole
// image, the "Snek" title in the upper part, and "Loading..." below it.
fun splash(p: Palette, tile: Raster) = Raster(640, 400).apply {
    val c = canvas
    c.tileFill(tile.image(), 0.0, 0.0, 640.0, 400.0, 0.75)
    c.drawImage(boardFrame(p).resized(640, 400, Resample.LANCZOS).toImage(), 0f, 0f)
    c.outlinedText("Snek", 320.0, 230.0, 160.0, p, align = Align.CENTER)
    c.outlinedText("Loading...", 320.0, 340.0, 34.0, p, align = Align.CENTER)
}
