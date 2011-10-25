@ECHO OFF
IF "%SCALA_HOME%"=="" (
  ECHO "'SCALA_HOME' IS NOT DEFINED, EXITING"
) ELSE (
  ECHO "Launching VisualLangLab ..."
  java -D"java.util.Arrays.useLegacyMergeSort=true" -cp ".;VLLS.jar;%SCALA_HOME%\lib\scala-library.jar;%SCALA_HOME%\lib\scala-compiler.jar;%SCALA_HOME%\lib\scala-swing.jar" vll.gui.VllGui
)
