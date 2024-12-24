package com.example.currencyexchangeapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView dovizListView; // ListView elemanını tanımladık.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dovizListView = findViewById(R.id.dovizListView); // XML Dosyasındaki ListView elemanını bulup dovizListView değişkenine atadık.
        new DovizKurTask().execute(); // DovizKurTask sınıfını başlattık.
    }

    // Arkaplanda çalışıp XML'den döviz kuru bilgilerini çekecek AsyncTask sınıfımızı tanımladık.
    public class DovizKurTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... voids) {
            // ECB (Avrupa Merkez Bankası) döviz kuru XML dosyasının URL'si.
            String urlString = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
            // Döviz kurlarını tutacak bir ArrayList oluşturuluyor.
            ArrayList<String> dovizListesi = new ArrayList<>();
            try {
                URL url = new URL(urlString); // Belirtilen URL'den url nesnemizi yarattık.
                HttpURLConnection baglanti = (HttpURLConnection) url.openConnection(); // URL ile HTTPS bağlantısı kurduk.
                InputStream inputStream = baglanti.getInputStream(); // URL'den gelen veriyi okuduk.

                // XML verisini analiz edecek XMLPullParser nesnesini oluşturuyoruz.
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                // XML verisini analiz edecek XMLPullParser nesnesini oluşturuyoruz.
                XmlPullParser parser = factory.newPullParser();
                // XML verisini okuyacak olan parser'ı, bağlantıdan alınan veri akışına yönlendiriyoruz.
                parser.setInput(inputStream, null);

                // XML verisi üzerinde işlem yapacak olay türünü ve çekilen döviz kuru bilgilerini saklayacak değişkenler oluşturuldu.
                int olayTipi = parser.getEventType();
                String paraBirimiKodu = "";
                String paraBirimiKuru = "";

                // XML verisini okuma ve işleme döngümüzü oluşturduk.
                while (olayTipi != XmlPullParser.END_DOCUMENT) {
                    String etiketAdi = null;
                    switch (olayTipi) {
                        case XmlPullParser.START_TAG: // XML etiketleri arasında geçiş.
                            etiketAdi = parser.getName();
                            if (etiketAdi.equals("Cube")) {
                                // XML etiketindeki Currency özelliğini çekiyoruz.
                                String paraBirimi = parser.getAttributeValue(null, "currency");
                                // XML etiketindeki Rate özelliğini çekiyoruz.
                                String kur = parser.getAttributeValue(null, "rate");

                                if (paraBirimi != null && kur != null) {
                                    paraBirimiKodu = paraBirimi;
                                    paraBirimiKuru = kur;
                                    // Döviz bilgilerini dovizListesi listesine ekliyoruz.
                                    dovizListesi.add("Currency: " + paraBirimiKodu + ", Rate: " + paraBirimiKuru); // ArrayList'e ekleniyor
                                }
                            }
                            break;
                    }
                    olayTipi = parser.next(); // XML'deki okuma işlemimiz bitti.
                }

                // dovizListesi ArrayList'ini bir String[] diziye dönüştürüp, sonucu döndürüyoruz.
                return dovizListesi.toArray(new String[0]);

            } catch (Exception e) {
                e.printStackTrace();
                // Eğer bir hata meydana gelirse diye hata mesajı döndürüyoruz.
                return new String[]{"An error occurred while fetching the data."};
            }
        }

        // Arka planda yapılan işlemler tamamlandıktan sonra burada çekilen döviz kuru verilerini ListView'de göstermek için kullanıyoruz.
        @Override
        protected void onPostExecute(String[] sonuc) {
            super.onPostExecute(sonuc);
            // sonuc dizisini ListView'e eklemek için kullanıyoruz.
            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, sonuc);
            dovizListView.setAdapter(adapter);
        }
    }
}
