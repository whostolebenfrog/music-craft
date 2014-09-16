(defproject music-craft "0.0.1-SNAPSHOT"
  :description "FIXME: write description"

  :dependencies [[com.badlogicgames.gdx/gdx "1.3.1"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.3.1"]
                 [com.badlogicgames.gdx/gdx-box2d "1.3.1"]
                 [com.badlogicgames.gdx/gdx-box2d-platform "1.3.1"
                  :classifier "natives-desktop"]
                 [com.badlogicgames.gdx/gdx-bullet "1.3.1"]
                 [com.badlogicgames.gdx/gdx-bullet-platform "1.3.1"
                  :classifier "natives-desktop"]
                 [com.badlogicgames.gdx/gdx-platform "1.3.1"
                  :classifier "natives-desktop"]
                 [net.mikera/clisk "0.10.0"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/math.combinatorics "0.0.8"]
                 [play-clj "0.3.11"]]

  :source-paths ["src" "src-common"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :aot [music-craft.core.desktop-launcher]
  :main music-craft.core.desktop-launcher)
