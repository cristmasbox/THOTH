[![](https://jitpack.io/v/cristmasbox/THOTH.svg)](https://jitpack.io/#cristmasbox/THOTH)

# THOTH (Transliteration and Hieroglyph Output Textview Helper class)
An android library with a custom TextView for displaying Egyptian hieroglyphs by using the `GlyphX` code.\
**You can test the THOTH Library using the [THOTH Example App](https://github.com/cristmasbox/THOTH-Example-App).**

*This library is part of the [Egyptian Writer](https://github.com/cristmasbox/Egyptian_Writer) Android App.*

## Disclaimer
This library uses the `GlyphX` and the `MdC` code for encoding Hieroglyphs.

A library for converting GlyphX to MdC and back is stored here: [GlyphConverter](https://github.com/cristmasbox/GlyphConverter)

> [!TIP]
> **If you only want to calculate the Dimensions and Positions (`Bounds`) of each sign**, then you can use the `MAAT`-library:\
> [cristmasbox/MAAT](https://github.com/cristmasbox/MAAT)

## Implementation with jitpack
Add this to your `settings.gradle.kts` at the end of repositories:
```
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
  }
}
```
Then add this dependency to your `build.gradle.kts` file:
```
dependencies {
  implementation("com.github.cristmasbox:THOTH:2.0.4")
}
```
> [!NOTE]
> For the implementation for other build systems like `Groovy` see [here](https://jitpack.io/#cristmasbox/THOTH/)

## Implementation with `.aar` file
Download the `THOTH_debug_versionname.aar` file from this repository, create a `libs` folder in your project directory and paste the file there. Then add this dependency to your `build.gradle.kts` file:
```
dependencies {
  implementation(files("../libs/THOTH_debug_versionname.aar"))
}
```

> [!IMPORTANT]
> If you renamed the `.aar` file you also have to change the name in the dependencies

## Usage
### Usage in Layout XML
Put this code into your `layout.xml`:
```
<com.blueapps.thoth.ThothView
    android:id="@+id/thothView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:textSize="40sp"
    android:text="r:Z1-n:km-m-t:O49"
    app:backgroundColor="#fff"
    app:primarySignColor="#000"
    app:altTextColor="#000"
    app:altTextSize="20sp"
    app:altText="Km.t m in.t hapi"
    app:showAltText="true"
    app:verticalOrientation="middle"
    app:writingLayout="lines" />
```
Here I will explain all the possible xml Attributes:

- `android:textSize`: Height of one line of big hieroglyphs. *Default: `200px`*
- `android:text`: Hieroglyphic text to display. Encoded with `MdC`.
- `app:verticalOrientation`: This parameter can only have three values and defines the vertical position of smaller signs (like `n`): *Default: `middle`*
  - `top`: Put signs to the top of the line  *In the code this equals `0`*
  - `middle`: Center signs vertically        *In the code this equals `1`*
  - `bottom`: Drop signs on Baseline         *In the code this equals `2`*
- `app:writingLayout`: This parameter only have two possible values and determines if signs should be written in lines or in columns: *Default: `lines`*
  - `lines`: Write signs in lines            *In the code this equals `0`*
  - `columns`: Write signs in columns        *In the code this equals `1`*
- `app:altText`: Alternative text which is displayed when hieroglyphs are loaded into memory.
- `app:showAltText`: Determines whether the alternative text should be displayed or not. *Default: `true`*
- `app:altTextSize`: Sets the text size of the alternative Text. *Default: 1/2 of the `android:textSize`*
- `app:altTextColor`: Defines the color of the alternative Text. *Default: `#000000`*
- `app:primarySignColor`: Defines the color of the hieroglyphs. *Default: `#000000`*
- `app:backgroundColor`: Defines the background color of the view. *Default: `transparent`*

### Changing values at runtime
To change the Attributes during runtime, you can call the `getter` and `setter` for the values. For example:
```
binding.thothView.setTextSize(200);     // Set the value for the textSize in pixels
```

You can also use some other functions which are explained here:

- `getGlyphXText()`: Returns the hieroglyphic text as `GlyphX`-String.
- `getMdCText()`: Returns the hieroglyphic text as `MdC`-String.
- `isAltTextTested()`: Returns whether the view is in `AltTextTesting`-Mode or not.
- `getLineThickness()`: Returns the thickness of the lines drawn between the columns / lines of the text in pixels
- `isDrawLines()`: Returns whether there should be lines drawn between the columns / lines of the text
- `getPagePaddingLeft()`: Returns the left padding of the text as a whole
- `getPagePaddingTop()`: Returns the top padding of the text as a whole
- `getPagePaddingRight()`: Returns the right padding of the text as a whole
- `getPagePaddingBottom()`: Returns the bottom padding of the text as a whole
- `getSignPadding()`: Returns the padding between signs outside of groups
- `getLayoutSignPadding()`: Returns the padding between signs inside a group
- `getInterLinePadding()`: Returns the padding between the lines / columns of the text

- `setGlyphXText(String glyphX)`: Change the hieroglyphic text during runtime by transferring the text as `GlyphX`-String.
- `setGlyphXText(org.w3c.dom.Document glyphX)`: Change the hieroglyphic text during runtime by transferring the text as `GlyphX`-XML-Document.
- `setMdCText(String mdc)`: Change the hieroglyphic text during runtime by transferring the text as `MdC`-String.
- `testAltText(boolean b)`: Enable or disable `AltTextTesting`-Mode. If `AltTextTesting`-Mode is enabled, the hieroglyph will not be rendered
and the view will act like if the hieroglyphs are currently loaded into memory. This is useful for testing how the alternative text looks like.
- `setLineThickness(float lineThickness)`: Sets the thickness of the lines drawn between the columns / lines of the text in pixels
- `setDrawLines(boolean drawLines)`: Determines if there should be drawn lines between the columns / lines of the text
- `setPagePaddingLeft(float pagePaddingLeft)`: Sets the left padding of the text as a whole
- `setPagePaddingTop(float pagePaddingTop)`: Sets the top padding of the text as a whole
- `setPagePaddingRight(float pagePaddingRight)`: Sets the right padding of the text as a whole
- `setPagePaddingBottom(float pagePaddingBottom)`: Sets the bottom padding of the text as a whole
- `setSignPadding(float signPadding)`: Sets the padding between signs outside of groups
- `setLayoutSignPadding(float layoutSignPadding)`: Sets the padding between signs inside a group
- `setInterLinePadding(float interLinePadding)`: Sets the padding between the lines / columns of the text

> [!NOTE]
> Currently the lines between the columns / lines of the texts are not drawn

> [!NOTE]
> Currently the view isn't displaying the alternative text correctly.\
> Especially when the `altTextSize` is very big, the text is not centered vertically.

## Version Catalog
### 05.10.2025@1.0.0
This is the first release of the THOTH library.
### 06.10.2025@1.0.1
Databases are updated.
Now it supports numbers as ids. This means you cant type in 
```
<sign id="500"/>
```
instead of typing
```
<v><h><sign id="V1"/><sign id="V1"/><sign id="V1"/></h><h><sign id="V1"/><sign id="V1"/></h></v>
```
### 26.10.2025@1.1.1
Support for MdC input added using the [GlyphConverter](https://github.com/cristmasbox/GlyphConverter) library.
### 08.11.2025@2.0.0
Support for brackets in MdC added. Now you can type in:
```
N17:i*(p:t)*(t:p)*i:N17
```
### 08.11.2025@2.0.1
Updated dependencies.
### 23.11.2025@2.0.2
- Function for directly passing GlyphX XML document to ThothView.java added. Also `getGlyphXText()` now returns a `org.w3c.dom.Document`.
  Now you can use:
  ```
  setGlyphXText(myXMLDocument);

  String content = getGlyphXTextString();
  Document content = getGlyphXText();
  ```
- Support for SDK `23` added.
### 11.12.2025@2.0.3
- Issue with multiple render requests solved *(e.g. if you are changing the text two times shortly after each other)*
- Support for `maat:1.5.1` added:
    - adding Paddings between lines, signs, in groups or around the text as a whole is possible now
    - `RTL`-layout is supported and the signs are mirrored
    - multiline texts are possible with the `!` and `!!` sign in `MdC` and with `<br/>` and `<pbr/>` in `glyphX`
### 16.02.2026@2.0.4
Now it uses the [SignProvider-Library](https://github.com/cristmasbox/SignProvider) only.
### latest Version
`16.02.2026@2.0.4`
