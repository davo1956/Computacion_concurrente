class Account {
   private int id = -1;  private String name = null;
   private int savings = -1, checking = -1;

   public Account(int id, String name, int savings, int checking) {
      this.id = id;  this.name = name;
      this.savings = savings;  this.checking = checking;
   }

   public String toString() {
      return "account id=" + id + ", name=" + name + ", savings=" + savings
         + ", checking=" + checking;
   }

   public int getId() { return id; }

   public String getName() { return name; }
   public void setName(String n) { name = n; }

   public int getSavings() { return savings; }
   public void setSavings(int i) { savings = i; }

   public int getChecking() { return checking; }
   public void setChecking(int i) { checking = i; }
}

class Bank {
   private static int nextBankID = 0;

   private int bankID = -1;
   private String name = null;

   // private if not subclassed, else give subclass access with protected
   protected int numCustomers = 0;
   protected Account[] account = null;

   public Bank(String name) { this(name, 10, 1000); }

   public Bank(String name, int numCustomers, int initialSavings) {
      this(name, numCustomers, initialSavings, true);
   }

   // for use by subclasses so they can do their own account initialization
   protected Bank(String name, int numCustomers, int initialSavings,
         boolean initialize) {
      this.name = name;
      this.numCustomers = numCustomers;
      bankID = nextBankID++;
      if (initialize) initializeAccounts(initialSavings);
   }

   private void initializeAccounts(int initialSavings) {
      account = new Account[numCustomers];
      for (int i = 0; i < numCustomers; i++) {
         String n = "Pat Doe";  int s = initialSavings;  int c = 0;
         account[i] = new Account(i, n, s, c);
      }
   }

   public static int getNextBankID() { return nextBankID; }

   protected boolean validCustomerID(int customerID) {
      return customerID >=0 && customerID < numCustomers;
   }

   public String getBankName() { return name; }

   public final int getBankID() { return bankID; }  // no overriding

   public int getNumCustomers() { return numCustomers; }

   public int getBalance(int customerID) {
      if (validCustomerID(customerID))
         return account[customerID].getSavings();
      else return -1;
   }

   public String toString() {
      return name + " Bank (bankID = " + bankID + ") has "
         + numCustomers + " customers";
   }

   public boolean withdraw(int customerID, int amount) {
      if (validCustomerID(customerID)
            && amount < account[customerID].getSavings()) {
         account[customerID].setSavings(
            account[customerID].getSavings() - amount);
         // ... log the transaction into the accounting records
         return true;
      } else return false;
   }

   public boolean deposit(int customerID, int amount) {
      if (validCustomerID(customerID)) {
         account[customerID].setSavings(
            account[customerID].getSavings() + amount);
         // ... log the transaction into the accounting records
         return true;
      } else return false;
   }

   public boolean transfer(int fromCustomerID, int toCustomerID,
         int amount) {
      if (!validCustomerID(toCustomerID)) return false;
      if (!withdraw(fromCustomerID, amount)) return false;
      // deposit cannot return false, so no worry about undoing withdraw
      deposit(toCustomerID, amount);
      // ... log the transaction into the accounting records
      return true;
   }

   public String statement(int customerID) {
      if (validCustomerID(customerID)) {
         // ... log the transaction into the accounting records
         return name + ": " + account[customerID];
      } else return null;
   }
}

class Neighborhood {

   public static void main(String[] args) {
      Bank national = new Bank("National");
      Bank peoples = new Bank("Peoples", 100, 10000);
      System.out.println("This neighborhood has two banks:\n "
         + national + "\n " + peoples);
      System.out.println("The next bank created will have ID "
         + Bank.getNextBankID());
      System.out.println(national.getBankName()
         + " has ID " + national.getBankID() + " and "
         + national.getNumCustomers() + " customers");
      System.out.println(peoples.getBankName()
         + " has ID " + peoples.getBankID() + " and "
         + peoples.getNumCustomers() + " customers");
      // perform some transactions ...
      if (!national.withdraw(3, 100))
          System.out.println("National cannot withdraw $100 from account 3");
      if (!peoples.deposit(7, 99))
          System.out.println("Peoples cannot deposit $99 to account 7");
      if (!national.deposit(42, 100))
          System.out.println("National cannot deposit $100 to account 42");
      if (!peoples.transfer(17, 19, 99000)) System.out.println
          ("Peoples cannot transfer $99000 from account 17 to account 19");
      System.out.println(national.statement(3));
      System.out.println(peoples.statement(7));
      System.out.println(national.statement(42));
      System.out.println(peoples.statement(17));
      System.out.println(peoples.statement(19));
   }
}

/* ............... Example compile and run(s)

% javac bank.java

% java Neighborhood
This neighborhood has two banks:
 National Bank (bankID = 0) has 10 customers
 Peoples Bank (bankID = 1) has 100 customers
The next bank created will have ID 2
National has ID 0 and 10 customers
Peoples has ID 1 and 100 customers
National cannot deposit $100 to account 42
Peoples cannot transfer $99000 from account 17 to account 19
National: account id=3, name=Pat Doe, savings=900, checking=0
Peoples: account id=7, name=Pat Doe, savings=10099, checking=0
null
Peoples: account id=17, name=Pat Doe, savings=10000, checking=0
Peoples: account id=19, name=Pat Doe, savings=10000, checking=0
                                            ... end of example run(s)  */
