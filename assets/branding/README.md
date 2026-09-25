# GOST Branding Assets

`gost-mark-source.png` is the raster source for the approved logo direction, extracted and cleaned from the supplied brand board using image generation. It is not a pixel-identical vector tracing of the board. Preserve its ribbon shading and proportions; do not substitute the earlier approximate SVG paths.

## Regenerate

On macOS, with ImageMagick and librsvg installed:

```bash
bash assets/branding/regenerate-icons.sh
```

The script normalizes the transparent mark onto a 1024px canvas, copies it to `composeApp/src/jvmMain/composeResources/drawable/gost_brand_mark.png`, composites the mark over `gost-icon-background.svg`, and generates the PNG, ICNS and ICO package icons.

## Outputs

- `gost-mark-1024.png`: transparent mark used inside the application.
- `gost-app-icon-1024.png`: rounded-square icon for application packaging.
- `../../composeApp/icons/gost.png`, `gost.icns`, `gost.ico`: platform assets.

The Compose accessor is `Res.drawable.gost_brand_mark`. The package icon retains the separate `gost` resource ID. Avoid duplicate basenames with different extensions in the same Compose drawable directory.
