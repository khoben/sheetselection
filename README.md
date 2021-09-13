# Sheet Selection
[![Jitpack SheetSelection](https://jitpack.io/v/khoben/sheetselection.svg)](https://jitpack.io/#khoben/sheetselection)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/minibugdev/DrawableBadge/master/LICENSE)

## Based on [minibugdev/SheetSelection](https://github.com/minibugdev/SheetSelection/tree/d6958f9a4ebe95eca539c9edc098d648ecd0d177) with some improvements:
1. Multi-selection
2. Retained selection callback
3. and more little changes...

## How to use
### [Sample app](app\src\main\java\com\khoben\sheetselection\sample\MainActivity.kt)

## Screenshots
<table>
    <td>
        <p align="center"><img height="400px" src="./README.md-images/1.png"></p>
    </td>
     <td>
        <p align="center"><img height="400px" src="./README.md-images/2.png"></p>
    </td>
</table>

## Installation
Add it in your **root** `build.gradle` at the end of repositories:
``` groovy
repositories {
    ..
    maven { url 'https://jitpack.io' }
}
```
Add the dependency
``` groovy
dependencies {
    implementation 'com.github.khoben:sheetselection:1.0.0'
}
```