(require 'projectile)
(require 'cider)

(defun elo-clojure-repl? ()
  (not (null (member cider-repl-type '(clj "clj")))))

(defun elo-start-finops-admin ()
  (interactive)
  (cider-switch-to-repl-buffer)
  (when
      (and
       (elo-clojure-repl?)
       (equal (projectile-project-name) "elo"))

    (insert "(ir/go)")
    (cider-repl-return)))

(add-hook 'cider-connected-hook 'elo-start-finops-admin)

(provide 'elo-auto-go)
