class MiHilo implements Runnable{
    Thread hilo;

    //Construye un nuevo hilo.
     MiHilo(String nombre){
         hilo= new Thread(this,nombre);
     }

     //Un método de fábrica que crea e inicia un hilo.

     public static MiHilo crearYComenzar (String nombre){
         MiHilo miHilo=new MiHilo(nombre);
         miHilo.hilo.start(); //Inicia el hilo
         return miHilo;
     }

     //Punto de entrada de hilo.
     public void run(){
         System.out.println(hilo.getName()+" iniciando.");
         try {
             for (int count=0; count<10;count++){
                 Thread.sleep(400);
                 System.out.println("En "+hilo.getName()+ ", el recuento es "+count);
             }
         }catch (InterruptedException exc){
             System.out.println(hilo.getName()+ " interrumpudo.");
         }
         System.out.println(hilo.getName()+" terminado.");
     }
 }

 class MasHilos {
    public static void main(String[] args) {
        System.out.println("Hilo principal iniciando.");

        MiHilo miHilo1 = MiHilo.crearYComenzar("#1");
        MiHilo miHilo2 = MiHilo.crearYComenzar("#2");
        MiHilo miHilo3 = MiHilo.crearYComenzar("#3");

        for (int i = 0; i < 50; i++) {
            System.out.print(".");
            try {
                Thread.sleep(100);
            } catch (InterruptedException exc) {
                System.out.println("Hilo principal interrumpido.");
            }
        }
        System.out.println("Hilo principal finalizado");
    }
}