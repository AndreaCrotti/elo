(ns byf.pages.utils
  (:require [byf.config :refer [value]])

  (:import (java.util UUID)))

(defn cache-buster
  [path]
  ;; fallback to a random git sha when nothing is found
  (format "%s?git_sha=%s"
          path
          (or (value :git-commit) (str (UUID/randomUUID)))))
