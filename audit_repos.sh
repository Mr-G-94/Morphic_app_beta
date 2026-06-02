#!/bin/bash
# audit_repos.sh
# Uso: ./audit_repos.sh

PARENT_DIR="."
CHILD_DIR="Morphic_app_beta"

echo "=========================================================="
echo "AUDITORÍA DE REPOSITORIOS MORPHIC APP"
echo "=========================================================="

# 1. Verificar estado contra GitHub
check_git_status() {
    echo ">> Analizando repositorio en: $1"
    cd "$1" || return
    git fetch origin
    echo "Estado local vs GitHub:"
    git status -sb
    cd ..
}

echo "[1/2] Analizando Repositorio PADRE..."
check_git_status "$PARENT_DIR"

echo -e "\n[2/2] Analizando Repositorio HIJO..."
check_git_status "$CHILD_DIR"

echo -e "\n=========================================================="
echo "COMPARATIVA DE ARCHIVOS (Ignorando .git y carpetas de build)"
echo "=========================================================="
# Compara recursivamente, ignora .git y carpetas build de gradle
diff -rq "$PARENT_DIR" "$CHILD_DIR" \
    --exclude=.git \
    --exclude=build \
    --exclude=.gradle \
    --exclude=app/build \
    --exclude=Morphic_app_beta

echo -e "\n--- Auditoría terminada ---"
