(require 'projectile)
(require 'cider)

(defun byf-clojure-repl? ()
  (not (null (member cider-repl-type '(clj "clj")))))

(defun byf-start-finops-admin ()
  (interactive)
  (cider-switch-to-repl-buffer)
  (when
      (and
       (byf-clojure-repl?)
       (equal (projectile-project-name) "elo"))

    (insert "(ir/go)")
    (cider-repl-return)))

(add-hook 'cider-connected-hook 'byf-start-finops-admin)

(provide 'byf-auto-go)
