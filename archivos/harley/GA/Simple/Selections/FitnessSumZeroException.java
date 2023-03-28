package Selections;

public class FitnessSumZeroException extends SelectionException {
   FitnessSumZeroException() {
      super("ProportionalSelection: population fitness sums to zero");
   }
}
