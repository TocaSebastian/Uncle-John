import java.io.*;
import java.util.*;
import java.net.*;

class Ferma  extends Thread{
  int N; // dimensiunea matricei din ferma
  int mat[][]; // matricea unde sunt pozitionate gainile si unde se gasesc ouale 
               //    0 - nu este gaina
               //    2 - este gaina
               //    1 - este ou
  int oua[][]; // matricea unde sunt pozitionate ouale in ferme
  int nr_gaini; // numarul gainilor din ferma
  Gaina gaini[];  // gainile din ferma
  int timp_monitorizare; // timp monitorizare ferma  
  int ferma_nr; // numarul fermei
  boolean ou_depus; // s-a depus cel putin un ou in ferma
  int angajat_nr; // numarul angajatul care aduna ouale
  FileOutputStream g;
  PrintStream gchar;
  String path_file;

/////////////////////////////////////////////////////////////
//  Definirea constructorului clasei Ferma
//////////////////////////////////////////////////////////// 
  public Ferma(int ferma_nr, int n, int vg[], int ng1, int ng2)  //Ferma(int numarul_fermei, int dim_N, int vectorul_gaini, int ng1, int ng2)  unde ng1,ng2 partea de inceput si sfarsit din vectorul_gaini
  {
    this.N=n; // aloc dimnesiunea fermei, numarul N
    this.ferma_nr=ferma_nr;
    this.nr_gaini= ng2-ng1; // aloc numarul de gaini
    this.ou_depus= false; // initial nu avem oua depuse in ferma
    int v[]=new int[nr_gaini]; // Creare vector cu gainile alese aleator
    int j=0;
    System.out.print("\nFerma nr."+ferma_nr+" are "+nr_gaini+" gaini. [ ");
    for(int i=ng1;i<ng2;i++)
    {
      System.out.print((vg[i]+1)+" ");
      v[j++]=vg[i];
    }
    System.out.println(" ]"); 
    mat=new int[n][n];
    oua=new int[n][n];
    gaini=new Gaina[nr_gaini];
    Initializare_Ferma();      
    Aranjare_Gaini(v);
  } 
  /////////////////////////////////
  public Ferma() {  }
  /////////////////////////////////
  public Ferma get_Ferma()
 {
   Ferma f=new Ferma();
   f.ferma_nr=this.N; // dimensiunea matricei din ferma
   f.mat= this.mat; // matricea unde sunt pozitionate gainile si unde se gasesc ouale 
   f.oua=this.oua; // matricea unde sunt pozitionate ouale in ferme
   f.nr_gaini=this.nr_gaini; // numarul gainilor din ferma
   f.gaini=this.gaini;  // gainile din ferma
   f.timp_monitorizare=this.timp_monitorizare; // timp monitorizare ferma  
   f.ferma_nr=this.ferma_nr; // numarul fermei
   f.ou_depus=this.ou_depus;
   return f;
 }
  
  
  ////////////////////////////////////////////////////////////////////////////
  void Initializare_Ferma()  // la inceput nu avem gaini pozitionate in ferma si nici oua in ferma
  {
    for(int i=0;i<N;i++)
       for(int j=0; j<N; j++)
        {
          mat[i][j]=0;
          oua[i][j]=0;
        }
  }
  
  ////////////////////////////////////////////////////////////////
  void Aranjare_Gaini(int vg[])  // asezam aleator gainile in ferma
  {
    int x=0, y=0;    
    Random random = new Random();
    for(int i=0;i<nr_gaini;i++)
    {
      for(int j=0;j<i;j++)
      {
        x=random.nextInt(N);
        y=random.nextInt(N);
        if((gaini[j].get_x()==x)&&(gaini[j].get_y()==y))
            j=0; // daca se suprapune o gaina peste alta anterioara generez o noua pozitie
      }
      gaini[i]= new Gaina(x, y,vg[i]+1); 
      gaini[i].start(); // pornesc firul pentru fiecare gaina
      mat[x][y]=vg[i]+1; // asez gaina nr [i] in matricea fermei
    }
  }
  
  /////////////////////////////////////////////////////////////////////////////
  public synchronized void afisare_Ferma() throws InterruptedException
  {
    // wait();
     System.out.println("\nFerma nr."+this.ferma_nr);     
     for(int i=0;i<N;i++)
     {
       for(int j=0;j<N;j++)
          System.out.print(mat[i][j]+" ");
       System.out.println();
     }
  //   notifyAll();
  }   
  
  public void afisare_vector_oua() //throws IOException
  {
    System.out.println("\nSe colecteaza ouale de la Ferma nr."+this.ferma_nr); 
    path_file="angajat"+this.angajat_nr+".txt";
    System.out.println(path_file);
    try{
      g=new FileOutputStream(path_file);
    }catch (Exception e) {  }
     gchar=new PrintStream(g);
      
    String linie;
    linie=""+N; 
    try
    {
      gchar.println(linie); // scrie in fisier dim matricei NxN
      linie=""+this.ferma_nr;  
      gchar.println(linie); // scrie numarul fermei din care se aduna ouale
      int oua_colectate=0;  
      for(int i=0;i<N;i++)
      {
        linie="";
        for(int j=0;j<N;j++)
        {
         linie=linie+" "+oua[i][j];
         oua_colectate+=oua[i][j];
         oua[i][j]=0; // dupa transferul oualelor catre angajat se goleste ferma de oua
        }
        gchar.println(linie);  // se scrie o linia a matricei
        System.out.println("Au fost colectate "+oua_colectate+" oua.");  
      }
    }catch (Exception e) {  }
    finally { }
    gchar.close();
  }   
  
  public boolean verifica_poz(int x, int y)  // return TRUE daca este libera casuta unde se deplaseaza gaina este libera (=0)
  {
    boolean ok=true;
    System.out.println("x="+x+" y="+y);
    if((x>=N)||(y>=N)||(x<0)||(y<0))  // verific sa nu depaseasca limitele matricei (a fermei)
          ok=false;
    for(int i=0;i<N && ok;i++)
    {
       for(int j=0;j<N && ok;j++)
          if((i==x)&&(j==y)&&(mat[i][j]!=0))
          {
              ok=false; // mai exista pe pozitia X,Y inca o gaina       
              
          }
    }
    return ok;
  }
  
   public synchronized void deplasare_gaina() throws InterruptedException
  {
     
     for(int i=0;i<nr_gaini;i++)
        if(gaini[i].ou_depus ==1 ) // daca gaina[i] a depus un ou atunci se deplaseaza
        {
          ou_depus=true; // avem un ou depus in ferma
          // deplaseaza gaina
          System.out.println("Deplasare gaina "+gaini[i].gaina_nr+" in Ferma nr."+this.ferma_nr);
          //gaini[i].wait();
          oua[gaini[i].get_x()][gaini[i].get_y()]=1;
          boolean ok=false;
          while(!ok)   // repeta pana gasesc o pozitie posibila
          {
            //seteaza noile pozitii ale gaini
            int xy[]=gaini[i].deplasare_gaina();
            if (verifica_poz(xy[0],xy[1]))
            {
                ok = true; // am o posibila deplasare a gainii
                //gaini[i].notifyAll();
                mat[gaini[i].get_x()][gaini[i].get_y()]=0;
                gaini[i].set_x(xy[0]);
                gaini[i].set_y(xy[1]);
                mat[xy[0]][xy[1]]=gaini[i].gaina_nr;
            }
            else{
                  System.out.println("gaina este inconjurata de alte gaini, asteapta un timp aleatoriu `t`.");
                  sleep( (int) (Math.random() * 40)+10 ); // daca gaina este inconjurata de alte gaini si nu se poate misca in nicio directie, 
                                                          // asteapta aleatoriu t, unde 10 <= t <= 50 milisecunde pana la urmatoarea mutare posibila
  
            }
 
          }
          
        }
     
  }
  /////////////////////////////////////////////////////////////////////////////
  // se executa firul pentru fiecare ferma
  ////////////////////////////////////////////////////////////////////////////
  public void run()
  { 
    try
    { 
      while( true )
      { 
        deplasare_gaina();
        //afisare_Ferma(); 
        System.out.println("La fiecare 5 secunde sistemul solicita pozitia gainilor in interiorul Fermei nr."+this.ferma_nr);
        sleep( (int) (Math.random() * 5000) ); // La fiecare 5 secunde sistemul de monitorizare a fermei va solicita pozitia gainilor in interiorul fermei
        afisare_Ferma(); 
      } 
    } 
    catch( InterruptedException e ){ } 
 }                                                        
  
public void set_ou_depus(boolean val) 
{
    ou_depus=val;
}

public boolean get_ou_depus()
{
   return(ou_depus); 
}

public void set_angajat_nr(int nr)
{
  this.angajat_nr=nr;  
}  
 ////////////////////////////////
}// clasa Ferma
 ////////////////////////////////




////////////////////////////////////////////////////////////////////////////////
// Clasa Gaina
////////////////////////////////////////////////////////////////////////////////
class  Gaina extends Thread{
  int timp_ou;   // timp depunere oua aleator
  int pozitie_x,pozitie_y;  // pozitia unde se afla gaina
  //int N; // dimensiunea cadrului in care se deplaseaza gaina
  int ou_depus=0; // daca a depus oul sau nu
  int gaina_nr; // numarul gainii din ferme
  
////////////////////////////////////////////////////////////////////////////////
// Definirea constructorului/ constructorilor clasei Gaina
  public Gaina(int x, int y, int t, int ng)   // initializare gaina (asazarea gainii pe coordonate si stabilirea timpului de depunere oua)
  {
    this.pozitie_x=x;
    this.pozitie_y=y;
    this.timp_ou=t + 500; // timpul de depunere oua ( 500<= t <= 1000) 
    this.gaina_nr=ng;
  }
  
  public Gaina(int x, int y, int ng)   // initializare gaina (asazarea gainii pe coordonate) 
  {
    this.pozitie_x=x;
    this.pozitie_y=y;
    Random random = new Random();
    this.timp_ou=500+random.nextInt(500); // stabilirea timpului de depunere oua
    this.gaina_nr=ng;
  }
  
  /////////////////////////////////////////////////////////////////////////////
  //  returneaza pozitia gainii pe axa X
  public int get_x()
  {
      return this.pozitie_x;    
  }
  
  /////////////////////////////////////////////////////////////////////////////
  //  returneaza pozitia gainii pe axa Y
  public int get_y()
  {
      return this.pozitie_y;
  }
  
  /////////////////////////////////////////////////////////////////////////////
  //  returneaza pozitia gainii pe axa Y
  public int get_gaina_nr()
  {
      return this.gaina_nr;
  }
  
  /////////////////////////////////////////////////////////////////////////////
  //  seteaza pozitia gainii pe axa X
  public void set_x(int x)
  {
      this.pozitie_x=x;    
  }
  
  /////////////////////////////////////////////////////////////////////////////
  //  returneaza pozitia gainii pe axa Y
  public void set_y(int y)
  {
      this.pozitie_y=y;
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // trateaza firul de depunere a unui ou
  public synchronized void depune_ou() throws InterruptedException
  {
    System.out.println("Se depune un ou!  [Gaina nr. "+this.gaina_nr+"]. Asteapta 30ms.");
    ou_depus=1;
    sleep( 30 ); // dupa ce se creeaza un ou, gaina trebuie sa se odihneasca pentru a produce un alt ou (timp de 30 milisecunde)
    wait();
  }
  
  /*public int[] mut_x(int x,int y)
  {
    int x_nou=x+1;
    int y_nou=y;
    if(x_nou>=10)
                x_nou=0;
    return new int[] {x_nou, y_nou};
  }
  */
  
  public int[] deplasare_gaina()  // genereaza o posibila deplasare
  {
    Random random = new Random();
    int x_nou=this.pozitie_x+random.nextInt(3)-1;  // se genereaza o posibila deplasare -1(stamga) 0(ramane pe pozitie) 1(dreapta)
    int y_nou=this.pozitie_y+random.nextInt(3)-1;  // se genereaza o posibila deplasare -1(sus) 0(ramane pe poz) 1(jos)
    System.out.println("Gaina nr. "+this.gaina_nr+" se deplaseaza.");
    return new int[] {x_nou, y_nou};
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // se executa firul pentru fiecare gaina
  public void run()
  { 
    try
    { 
      while( true )
      { 
        ou_depus=0;
        //sleep( (int) (Math.random() * this.timp_ou) ); // asteapta sa depuna un ou
        sleep( (int) (Math.random() * 10000) );
        depune_ou();
      } 
    } 
    catch( InterruptedException e ){ } 
 } 

} // clasa Gaini


class Multi extends Thread
{
  private Socket s=null;
  DataInputStream infromClient;
  OutputStream out;
  BufferedWriter writer;
  int solicitare_angajat; // Angajatul solicita informatii despre ferma
  Ferma_John fj;
  int sunt_oua_la_ferma; // arata daca sunt oua depuse de gaini la ferma
  
Multi() throws IOException{}
  
Multi(Socket s,Ferma_John fj) throws IOException
{
    this.s=s;
    this.fj=fj;
    infromClient = new DataInputStream(s.getInputStream());
    out= new DataOutputStream(s.getOutputStream());
    writer = new BufferedWriter(new OutputStreamWriter(out));
}
public void run()
{   
    sunt_oua_la_ferma=0;
    try 
    {
      solicitare_angajat = (int)infromClient.read(); // solicitarea este transmisa catre ferma prin numarul angajatului
    } catch (IOException ex) { }
    System.out.println("\n[ Angajatul nr."+solicitare_angajat+" verifica daca sunt de colectat oua. ]"); 
    sunt_oua_la_ferma=fj.afisare_ferma_oua(solicitare_angajat); // returneaza nr. fermei de unde se vor colecta ouale 
    
    try {
      if(sunt_oua_la_ferma !=0)
        writer.write(1);//sunt_oua_la_ferma);
        else {
          writer.write(0);
        } // end of if-else
       writer.flush();
       writer.close();
       infromClient.close(); 
     } catch (IOException ex) {} 
    try {
        System.out.println("Socket Closing");
        s.close();
    } catch (IOException ex) {
        //Logger.getLogger(Multi.class.getName()).log(Level.SEVERE, null, ex);
       }
   }  
}
///////////////////////////////////////////////////////////////////////////////
// Clasa Ferma_John 
public class Ferma_John //extends Thread
{ 
  int vector_gaini[];
  int nr_gaini;  
  int nr_ferme;
  Ferma f[];
  
  public Ferma_John(){
  }

  public Ferma_John(int n) // Ferma_John( total_nr_gaini)
  {
     nr_gaini=n;
     aranjare_vector_gaini(n);
     afisare_vector_gaini();
     nr_ferme=3;
     
     f = new Ferma[nr_ferme];
    // Ferma(int numarul_fermei, int dim_N, int vectorul_gaini, int ng1, int ng2)  unde ng1,ng2 partea de inceput si sfarsit din vectorul_gaini
     f[0]=new Ferma(1,7,vector_gaini,0,5); // Ferma nr.1 are 7x7 dim si 5 gaini    
     f[1]=new Ferma(2,7,vector_gaini,5,11); // Ferma nr.2 are 7x7 dim si 6 gaini 
     f[2]=new Ferma(3,7,vector_gaini,11,15); // Ferma nr.3 are 7x7 dim si 4 gaini   
  } 
  
  public void StartFerma()
  {
     for(int i=0;i<nr_ferme;i++) // pornesc fiecare fir al Fermei `i`
       f[i].start();
  }
  
  public void aranjare_vector_gaini(int nr_gaini)
  {
    vector_gaini=new int[nr_gaini]; 
    //for(int i=0;i<nr_gaini;vector_gaini[i++]=i+1);
    Random random = new Random();
    for(int i=0;i<nr_gaini;i++)
    {
      vector_gaini[i]=random.nextInt(nr_gaini);
      for(int j=0;j<i;j++)
        if(vector_gaini[j]==vector_gaini[i])
        {
          vector_gaini[i]=random.nextInt(nr_gaini);
          j=0;
        }
    }                                    
  }
  
  public void afisare_vector_gaini()  
  {
      System.out.print("Vectorul gainilor: ");   
      for(int i=0;i<nr_gaini;i++)
        System.out.print((vector_gaini[i]+1)+"  ");
      System.out.println();
  }
  
  public int afisare_ferma_oua(int solicitare_angajat)
  {
    int sunt_oua_la_ferma=0;
    for(int i=0;i<nr_ferme;i++) 
       if(f[i].get_ou_depus())
       {
          f[i].set_angajat_nr(solicitare_angajat);
          f[i].afisare_vector_oua();
          if(f[i].get_ou_depus()) 
            {
              sunt_oua_la_ferma=i;
              break;
            }
        }
    return sunt_oua_la_ferma;
  }
  ////////////////////////////////////////////////////////////////////////////
  ////        SOCKET
  ///////////////////////////////////////////////////////////////////////////
   
  
  public static void main(String[] args)throws IOException, InterruptedException
  {
    Ferma_John fj=new Ferma_John(15);
    int port=900;
    System.out.println("Start Ferma Unchiului John");
    fj.StartFerma();
    while(true){
        ServerSocket ss=new ServerSocket(port);
        System.out.println("Serverul Asculta ....."); 
        Socket s=ss.accept();
        Multi t=new Multi(s,fj);
        t.start();
        ///////////////////
        Thread.sleep(2000); // fiecare fir asteapta 
        ss.close();
    }    
    /*
     int nr_ferme=1;
     Ferma f[];
     f = new Ferma[nr_ferme];
     f[0]=new Ferma(7,4);
     f[0].start(); 
     */  
  }
}
