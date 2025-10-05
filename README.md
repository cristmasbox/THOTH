[![](https://jitpack.io/v/cristmasbox/THOTH.svg)](https://jitpack.io/#cristmasbox/THOTH)

# THOTH (Transliteration and Hieroglyph Output Textview Helperclass)
An android library with a custom TextView for displaying Egyptian hieroglyphs by using the `GlyphX` code.

## Disclaimer
This library uses the `GlyphX` code for encoding Hieroglyphs.

A library for converting GlyphX to MdC and back is planned. If you only want to calculate the Dimensions and Positions (`Bounds`) of each sign, then you can use the `MAAT`-library:\
[cristmasbox/MAAT](https://github.com/cristmasbox/MAAT)

## Implemetation with jitpack
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
  implementation("com.github.cristmasbox:THOTH:1.0.0")
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

## Version Catalog
### 05.10.2025@1.0.0
This is the first release of the THOTH library.
### latest Version
`05.10.2025@1.0.0`
