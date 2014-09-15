(ns music-craft.core.desktop-launcher
  (:require [music-craft.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. music-craft "music-craft" 800 600)
  (Keyboard/enableRepeatEvents true))

(comment (-main))
