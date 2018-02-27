import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import javax.swing.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Parser extends SwingWorker<Boolean, String> {

    private String address = /*"https://grzegorzmatecki.staff.tcs.uj.edu.pl/java/test/"; */ "https://photojournal.jpl.nasa.gov/targetFamily/Jupiter" ;
    private volatile long sum = 0;

    Set<String> set = new HashSet<String>();

    Set<String> links = new HashSet<String>();

    private void setSum(long x){
        sum += x;
    }

    long getSum(){
        return sum;
    }

    void setAddress(String s){
        address = s;
    }

    String getAddress() {return address;}



    //public static void main(String[] args) throws IOException {
    public Boolean doInBackground() throws IOException {

            System.out.println("Coneccting " + address);

            org.jsoup.nodes.Document document = Jsoup.connect(address).get();

            System.out.println("Conected");

            long counter;

            for (Element e : document.select("a[href]")) {
                if (links.contains(e.attr("abs:href")))
                    continue;

                links.add(e.attr("abs:href"));
            }

            for (Element e : document.select("img")) {
                if (set.contains(e.absUrl("src")))
                    continue;

                set.add(e.absUrl("src"));
            }

            System.out.println(set.size());

            ExecutorService service = Executors.newCachedThreadPool();

            for (String link : set) {
                service.submit(() -> {
                    URL url = null;
                    try {
                        try {
                            url = new URL(link);
                        } catch (IOException exc){
                            url = new URL("https://" + link);
                        }
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        int err = 0;
                        System.out.println(link);
                        urlConnection.setRequestProperty("User-Agent", "Mozilla/4.76");
                        try {

                            urlConnection.setRequestMethod("HEAD");

                            setSum(urlConnection.getContentLengthLong());

                            urlConnection.getInputStream().close();

                        } catch (IOException ex) {
                            System.out.println(link + " " + err);
                            System.out.println(urlConnection.getResponseCode());
                        } finally {
                            publish(link);
                            if (urlConnection != null) {
                                ((HttpURLConnection) urlConnection).disconnect();
                            }
                        }
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                });
            }


            service.shutdown();

            try {
                service.awaitTermination(123456789, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                System.out.println("Err...");
            }

            System.out.println(String.format("%.2f MB", sum / (1024.0 * 1024.0)));

        return true;
    }
}
