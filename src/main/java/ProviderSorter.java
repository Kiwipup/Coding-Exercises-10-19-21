import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ProviderSorter {

    public static void main(String[] args) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        String providerCsv = "provider.csv";
        Path outputFolder = Paths.get("src/main/resources/providerCsvOutput");
        List<Provider> providerList = readProvidersFromCsv(providerCsv);
        sort(providerList, outputFolder);
    }

    private static List<Provider> readProvidersFromCsv(String providerCsv) throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try(InputStream is = classloader.getResourceAsStream(providerCsv)) {
            InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
            Reader reader = new BufferedReader(streamReader);
            CsvToBean<Provider> csvReader = new CsvToBeanBuilder(reader)
                    .withType(Provider.class)
                    .withSeparator(',')
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();
            return csvReader.parse();
        }
    }

    private static void sort(List<Provider> providerList, Path outputFolder) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        Map<String, List<Provider>> grouped = groupByInsuranceCompany(providerList);
        sortByFirstAndLastName(grouped);
        removeDuplicates(grouped);
        saveToInsCsvFiles(grouped, outputFolder);
    }

    private static void saveToInsCsvFiles(Map<String, List<Provider>> grouped, Path outputFolder) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        for (Map.Entry<String, List<Provider>> entry : grouped.entrySet()) {
            String csvFileName = entry.getKey().replaceAll(" ", "") + ".csv";
            Path csvFilePath = outputFolder.resolve(csvFileName);
            writeToCsv(entry.getValue(), csvFilePath);
        }
    }

    private static void writeToCsv(List<Provider> providers, Path path) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        try(FileWriter writer = new FileWriter(path.toFile())) {
            StatefulBeanToCsvBuilder<Provider> builder = new StatefulBeanToCsvBuilder<>(writer);
            StatefulBeanToCsv<Provider> csvWriter = builder
                    .build();
            csvWriter.write(providers);
        }
    }

    private static void removeDuplicates(Map<String, List<Provider>> grouped) {
        for (Map.Entry<String, List<Provider>> entry : grouped.entrySet()) {
            List<Provider> list = entry.getValue();
            LinkedHashMap<String, Provider> matched = new LinkedHashMap<>();
            ListIterator<Provider> iterator = list.listIterator();
            while (iterator.hasNext()) {
                Provider p = iterator.next();
                Provider found = matched.get(p.getUserId());
                if (found == null || p.getVersion() > found.getVersion())
                    matched.put(p.getUserId(), p);
            }
            list = new ArrayList(matched.values());
            grouped.replace(entry.getKey(), list);
        }
    }

    private static void sortByFirstAndLastName(Map<String, List<Provider>> grouped) {
        grouped.values().forEach(list -> {
            list.sort(Comparator.comparing(Provider::getFirstName).thenComparing(Provider::getLastName));
        });
    }

    private static Map<String, List<Provider>> groupByInsuranceCompany(List<Provider> providerList) {
        Map<String, List<Provider>> grouped = new HashMap<>();
        for (Provider p : providerList) {
            List<Provider> insCompanyProviders = grouped.computeIfAbsent(p.getInsuranceCompany(), (key) -> new ArrayList<>());
            insCompanyProviders.add(p);
        }
        return grouped;
    }
}
