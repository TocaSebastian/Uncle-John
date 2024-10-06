import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

class ReceiverClient extends Thread
{
  private String path_file;
  private BufferedReader from;
  private boolean connected;
  private boolean finish = false;
  private AngajatClient chat;
  int angajat_nr; 
  int v[][]; // matricea cu oua ale fermei
  int n; // dimensiunea matricei fermei NxN
  int ferma_nr; // numarul fermei de unde aduna angajatul ouale
  
  public ReceiverClient(AngajatClient chat, InputStream in, int angajat_nr)
  {
    from = new BufferedReader(new InputStreamReader(in));
    this.chat = chat;
    connected = true;
    this.angajat_nr=angajat_nr;
  }
  
  public void citire_fisier() throws IOException
  {
    //path_file="D:\\Works\\Proiect\\";
    path_file="angajat"+this.angajat_nr+".txt";
    System.out.println("Fisier: "+path_file);
    FileInputStream f=new FileInputStream(path_file);// fiecare angajat are un fisier unic din care citeste (aduna) ouale
    InputStreamReader fchar=new InputStreamReader(f);
    BufferedReader buf=new BufferedReader(fchar);
    int i,j;
    String linie;
    linie=buf.readLine();  // citesc dimensiunea matricei N
    n=Integer.parseInt(linie);
    linie=buf.readLine(); // citesc numarul fermei 
    ferma_nr=Integer.parseInt(linie);
    v=new int[n][n];
    StringTokenizer t=new StringTokenizer(linie);
    for(i=0;i<n;i++)
    {
         linie=buf.readLine(); // citesc cate o linie a matricei
         t=new StringTokenizer(linie);
         for(j=0;j<n;j++)
           v[i][j]=Integer.parseInt(t.nextToken());
    }
    fchar.close();
    try {
            Files.delete(Paths.get(path_file));
        } catch (IOException e){ }

   // File file =new File(path_file);
    //file.delete();
  }
  
  public void afisare_oua()
  {
    System.out.println("Se colecteaza ouale de la ferma nr."+ferma_nr);
    int oua_colectate=0;
    for(int i=0;i<n;i++)
    {
      for(int j=0;j<n;j++)
      {
        System.out.print(v[i][j]+" ");
        oua_colectate+=v[i][j];
        v[i][j]=0;
      }  
      System.out.println();  
    }
    System.out.println("Angajatul "+this.angajat_nr+" a colectat "+oua_colectate+" oua.");
  }
  
  public void run()
  {
    int rasp_intrebare;
    
    while (connected && !finish)
    {
      try{
        rasp_intrebare = 0;
        rasp_intrebare = (int)from.read(); // citeste informatia din socket (raspunsul la intrebare)
        
        if(rasp_intrebare!=0) // daca sunt oua in ferma
          {
            finish=true;
            //sleep( (int) (Math.random() * 400 ));
            System.out.println("Sunt oua in ferma de colectat.");  
            citire_fisier();
            afisare_oua();
          }
          else { System.out.println("Nu sunt inca oua.");          } // end of if-else
        
      }catch (IOException ex)
      {
        //System.out.println(ex.getMessage());
        connected = false;
        chat.stopThread();
      }
    }
  }
  
  public void stopThread()
  {
    if (finish)
      return;
    
    finish = true;
    
    interrupt();    
  }
}

//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
public class AngajatClient extends Thread
{
  private String address;
  private int port;
  private Socket socket;
  private boolean connected = false;
  
  private InputStream in;      
  private OutputStream out;
  
  private ReceiverClient receiver;
  private BufferedWriter writer;
  private boolean finish = false;
  private int angajat_nr;

  public AngajatClient(String address, int port, int angajat_nr)
  {
    this.address = address;                                    
    this.port = port;
    this.angajat_nr=angajat_nr;
  }
  
  public boolean connect()
  {
    try{
      socket = new Socket(address, port);
      in = socket.getInputStream();
      out = socket.getOutputStream();
      
      writer = new BufferedWriter(new OutputStreamWriter(out));      
      receiver = new ReceiverClient(this, in, this.angajat_nr);
      receiver.start();
      
      connected = true;
      return connected;
    }catch (UnknownHostException ex)
    {
      //System.out.println(ex.getMessage());      
    }catch (IOException ex)
    {
       //System.out.println(ex.getMessage());
    }
    finally{
      return connected;
    }   
  }
  
  public void run()
  {    
    while (connected && !finish)
    {
      try{
        //System.out.println(this.angajat_nr);
        writer.write(this.angajat_nr);// angajatul nr solicita sa adune oua de la ferma
        writer.flush();
        //writer.close();
      }catch (IOException ex)
      {
        stopThread();
        connected = false;
        break;
      }
    }
  }
  
  public void stopThread()
  {
    finish = true;
    
    try{
      out.close();
    }catch (IOException ex){}
    
    try{
      in.close();
    }catch (IOException ex){}
    
    try{
      socket.close(); 
    }catch (IOException ex){}   
  }
  
  public static void main (String[] args)
  {
    int port=900;
    int numarul_angajati=3;
    AngajatClient angajati[]= new AngajatClient[numarul_angajati];
    
    while(true)
    try
    {
        for(int i=0;i<numarul_angajati;i++)
          angajati[i] = new AngajatClient("127.0.0.1", port,i+1);
        for(int i=0;i<numarul_angajati;i++)
        {
          if(angajati[i].connect()) 
            angajati[i].start(); 
          angajati[i].sleep( (int) (Math.random() * 2000));
        } 
    }catch(Exception e) { }
  } 
}
