class CommercialAccount extends Account {
   private int loan = -1;   // Commercial Banks make loans, too

   public CommercialAccount(int id, String name, int savings, int checking,
         int loan) {
      super(id, name, savings, checking);
      this.loan = loan;
   }

   public String toString() {  // override inherited method
      return super.toString() + ", loan=" + loan;
   }

   public int getLoan() { return loan; }
   public void setLoan(int i) { loan = i; }
}

final class CommercialBank extends Bank {  // no subclassing

   public CommercialBank(String name) { this(name, 10, 1000); }

   public CommercialBank(String name, int numCustomers, int initialSavings) {
      // invoke Bank constructor (super class)
      super(name, numCustomers, initialSavings, false);
      initializeAccounts(initialSavings);
   }

   private void initializeAccounts(int initialSavings) {
      // the account field is inherited from the super class
      account = new CommercialAccount[numCustomers];
      for (int i = 0; i < numCustomers; i++) {
         String n = "Pat Doe";  int s = initialSavings;  int c = 0;
         int l = 0;
         account[i] = new CommercialAccount(i, n, s, c, l);
      }
   }

   public String toString() {  // override inherited method
      return super.toString() + " and is Commercial";
   }

   public int getLoanBalance(int customerID) {  // new method
      if (validCustomerID(customerID))
         return ((CommercialAccount) account[customerID]).getLoan();
      else return -1;
   }

   // overload inherited method
   public boolean withdraw(int customerID, int amount, boolean overdraft) {
      if (withdraw(customerID, amount)) return true;
      else if (validCustomerID(customerID) && overdraft) {
         ((CommercialAccount) account[customerID]).setLoan(
            ((CommercialAccount) account[customerID]).getLoan() + amount);
         // ... log the transaction into the accounting records
         return true;
      } else return false;
   }
}

class City {

   public static void main(String[] args) {
      Bank mutual = new Bank("Mutual");
      Bank savings = new Bank("Savings", 100, 10000);
      CommercialBank merchants = new CommercialBank("Merchants", 100, 10000);
      System.out.println("This city has three banks:\n "
         + mutual + "\n " + savings + "\n " + merchants);
      System.out.println("Merchants customer 2 has a balance of "
         + merchants.getBalance(2));
      merchants.withdraw(2, 900);
      System.out.println("Merchants customer 2 has a balance of "
         + merchants.getBalance(2));
      System.out.println("Merchants customer 3 has a loan balance of "
         + merchants.getLoanBalance(3));
      merchants.withdraw(3, 100000, true);
      System.out.println("Merchants customer 3 has a loan balance of "
         + merchants.getLoanBalance(3));
      System.out.println(merchants.statement(2));
      System.out.println(merchants.statement(3));
   }
}

/* ............... Example compile and run(s)

% javac bank.java cbnk.java

% java City
This city has three banks:
 Mutual Bank (bankID = 0) has 10 customers
 Savings Bank (bankID = 1) has 100 customers
 Merchants Bank (bankID = 2) has 100 customers and is Commercial
Merchants customer 2 has a balance of 10000
Merchants customer 2 has a balance of 9100
Merchants customer 3 has a loan balance of 0
Merchants customer 3 has a loan balance of 100000
Merchants: account id=2, name=Pat Doe, savings=9100, checking=0, loan=0
Merchants: account id=3, name=Pat Doe, savings=10000, checking=0, loan=100000
                                            ... end of example run(s)  */
