(ns elo.pages.utils
  (:require [environ.core :refer [env]])

  (:import (java.util UUID)))

(defn cache-buster
  [path]
  ;; fallback to a random git sha when nothing is found
  (format "%s?git_sha=%s"
          path
          (:heroku-slug-commit env (str (UUID/randomUUID)))))
