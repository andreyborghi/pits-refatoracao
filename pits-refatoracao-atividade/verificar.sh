#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
modo="${1:-testes}"
case "$modo" in
  antes|refatorado|testes|todos) ;;
  *) echo 'Uso: bash verificar.sh [antes|refatorado|testes|todos]' >&2; exit 2 ;;
esac
command -v java >/dev/null || { echo 'É necessário um JDK 17 ou superior.' >&2; exit 1; }
mkdir -p build/classes
find antes/src refatorado/src testes/src -name '*.java' > build/fontes.txt
if command -v javac >/dev/null; then
  javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d build/classes @build/fontes.txt
else
  java -m jdk.compiler/com.sun.tools.javac.Main --release 17 -encoding UTF-8 \
    -Xlint:all -Werror -d build/classes @build/fontes.txt
fi
if [ "$modo" = testes ] || [ "$modo" = todos ]; then
  java -cp build/classes br.edu.pits.testes.CaracterizacaoLegadoTest
  java -cp build/classes br.edu.pits.testes.RegressaoTest
  java -cp build/classes br.edu.pits.testes.MelhoriasRefatoracaoTest
fi
if [ "$modo" = antes ] || [ "$modo" = todos ]; then
  java -cp build/classes br.edu.pits.app.MainLegado
fi
if [ "$modo" = refatorado ] || [ "$modo" = todos ]; then
  java -cp build/classes br.edu.pits.app.Main
fi
