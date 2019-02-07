# bird-watcher
An app for my local bird watching association.

## Video

<a href="http://www.youtube.com/watch?feature=player_embedded&v=ZKGj5zeGPAU
" target="_blank"><img src="http://img.youtube.com/vi/ZKGj5zeGPAU/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>


## General info

Bird data from Finnish bird atlas: http://atlas3.lintuatlas.fi/taustaa/kaytto

|||
|---|---|
|Built on SDK| 28|
|Min SDK | 14 |

## Technologies/concepts used

* Kotlin
* Dagger 2
* AndroidX
* Android Architecture Components
* Room Database
* RxJava 2
* Repository pattern
* Retrofit
* OkHttp Interceptor


## How is the species data loaded?

* Something subsribes to species data
* There's a check to see if data is fresh
* If fresh, give data from db. If not, update db from "api" and give data from db
* Api call is intercepted by OkHttp interceptor -> local json is given back (*Simulates real api*)


## How to build

1. Open `./BirdWatcher` in Android Studio
2. Build

## How to install (to compatible phone)

1. Download `bird-watcher-debug.apk`: https://github.com/vpuonti/bird-watcher/raw/master/bird-watcher-debug.apk on your phone
2. Install

## Screenshots

Screenshots and a short video can be found in `./screenshots`