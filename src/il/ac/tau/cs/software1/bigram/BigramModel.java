package il.ac.tau.cs.software1.bigram;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class BigramModel {
    public static final int MAX_VOCABULARY_SIZE = 14500;
    public static final String VOC_FILE_SUFFIX = ".voc";
    public static final String COUNTS_FILE_SUFFIX = ".counts";
    public static final String SOME_NUM = "some_num";
    public static final int ELEMENT_NOT_FOUND = -1;

    String[] mVocabulary;
    int[][] mBigramCounts;

    // DO NOT CHANGE THIS !!!
    public void initModel(String fileName) throws IOException {
        mVocabulary = buildVocabularyIndex(fileName);
        mBigramCounts = buildCountsArray(fileName, mVocabulary);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helper methods - already implemented for you. Use them!
    ///////////////////////////////////////////////////////////////////////////

    /*
     * Converts a single token (a whitespace-free piece of text from the file)
     * into the form in which it is stored in the vocabulary:
     *   - a token with at least one English letter  -> the token in lowercase
     *   - a token made only of digits (an integer)  -> SOME_NUM
     *   - anything else is not a legal word         -> null
     *
     * @pre: token != null
     */
    public static String normalizeWord(String token) {
        boolean hasLetter = false;
        boolean allDigits = !token.isEmpty();
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                hasLetter = true;
            }
            if (c < '0' || c > '9') {
                allDigits = false;
            }
        }
        if (hasLetter) {
            return token.toLowerCase(Locale.ROOT);
        }
        if (allDigits) {
            return SOME_NUM;
        }
        return null;
    }

    /*
     * @pre: vocabulary != null
     * @post: $ret = the index of word in vocabulary, or ELEMENT_NOT_FOUND if
     * word is null or does not appear in it
     */
    public static int findIndex(String[] vocabulary, String word) {
        if (word == null) {
            return ELEMENT_NOT_FOUND;
        }
        for (int i = 0; i < vocabulary.length; i++) {
            if (word.equals(vocabulary[i])) {
                return i;
            }
        }
        return ELEMENT_NOT_FOUND;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Questions 1 - 5: implement these
    ///////////////////////////////////////////////////////////////////////////

    /*
     * @post: mVocabulary = prev(mVocabulary)
     * @post: mBigramCounts = prev(mBigramCounts)
     */
    public String[] buildVocabularyIndex(String fileName) throws IOException { // Question 1
        String[] words = new String[MAX_VOCABULARY_SIZE];
        int count = 0;

        File fromFile = new File(fileName);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fromFile));
        String line;
        while ((line = bufferedReader.readLine()) != null && count < MAX_VOCABULARY_SIZE) {
            String[] tokens = line.trim().split("\\s+");
            for (String token : tokens) {
                String word = normalizeWord(token);
                if (word == null) {
                    continue;
                }
                if (count < MAX_VOCABULARY_SIZE && !isInVocabulary(words, word)) {
                    words[count] = word;
                    count++;
                }
            }
        }
        bufferedReader.close();

        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = words[i];
        }
        return result;
    }

    /*
     * @pre: words != null, and its non-null cells come first, with no gaps
     * @post: $ret = true iff word appears in words before the first null cell
     */
    private static boolean isInVocabulary(String[] words, String word) {
        int i = 0;
        while (i < MAX_VOCABULARY_SIZE && words[i] != null) {
            if (word.equals(words[i])) {
                return true;
            }
            i++;
        }
        return false;
    }

    /*
     * @post: mVocabulary = prev(mVocabulary)
     * @post: mBigramCounts = prev(mBigramCounts)
     */
    public int[][] buildCountsArray(String fileName, String[] vocabulary) throws IOException { // Question 2
        int n = vocabulary.length;
        int[][] counts = new int[n][n];

        File fromFile = new File(fileName);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fromFile));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] tokens = line.trim().split("\\s+");
            for (int i = 0; i < tokens.length - 1; i++) {
                String prevword = normalizeWord(tokens[i]);
                int k = findIndex(vocabulary, prevword);
                if (k != -1) {
                    String word = normalizeWord(tokens[i + 1]);
                    int m = findIndex(vocabulary, word);
                    if (m != -1) {
                        counts[k][m] += 1;
                    }
                }
            }
        }
        bufferedReader.close();
        return counts;
    }

    /*
     * @pre: the method initModel was called (the language model is initialized)
     * @pre: fileName is a legal file path
     */
    public void saveModel(String fileName) throws IOException { // Question 3
        String newLine = System.lineSeparator();

        // The vocabulary file
        BufferedWriter vocWriter = new BufferedWriter(new FileWriter(fileName + VOC_FILE_SUFFIX));
        vocWriter.write(mVocabulary.length + " words" + newLine);
        for (int i = 0; i < mVocabulary.length; i++) {
            vocWriter.write(i + "," + mVocabulary[i] + newLine);
        }
        vocWriter.close();

        // The counts file
        BufferedWriter countsWriter = new BufferedWriter(new FileWriter(fileName + COUNTS_FILE_SUFFIX));
        for (int j=0; j < mBigramCounts.length; j++){
            for(int t=0; t <mBigramCounts[j].length; t++){
                if (mBigramCounts[j][t]!=0){
                    countsWriter.write(j + "," + t + ":" + mBigramCounts[j][t] + newLine);
                }
            }
        }
        countsWriter.close();
    }

    /*
     * @pre: fileName is a legal file path
     */
    public void loadModel(String fileName) throws IOException { // Question 4
        // The vocabulary file
        BufferedReader vocReader = new BufferedReader(new FileReader(fileName + VOC_FILE_SUFFIX));
        String firstLine = vocReader.readLine();
        int n = Integer.parseInt(firstLine.split(" ")[0]);

        String[] vocabulary = new String[n];
        int[][] counts = new int[n][n];

        for (int i = 0; i < n; i++) {
            String line = vocReader.readLine();
            String[] parts = line.split(",", 2);
            int index = Integer.parseInt(parts[0]);
            vocabulary[index] = parts[1];
        }
        vocReader.close();

        BufferedReader countReader = new BufferedReader(new FileReader(fileName + COUNTS_FILE_SUFFIX));
        String line2;
        while ((line2 = countReader.readLine()) != null) {
            String[] sides = line2.split(":");
            String[] place = sides[0].split(",");
            int k = Integer.parseInt(place[0]);
            int m = Integer.parseInt(place[1]);
            int val = Integer.parseInt(sides[1]);
            counts[k][m]=val;
        }
        countReader.close();
        

        mVocabulary = vocabulary;
        mBigramCounts = counts;
    }

    /*
     * @pre: sentence is in lowercase
     * @pre: the method initModel was called (the language model is initialized)
     * @pre: each two words in the sentence are separated with a single space
     * @post: if sentence is probable, according to the model, $ret = true, else, $ret = false
     */
    public boolean isLegalSentence(String sentence) { // Question 5
        // TODO: implement
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Queries on the model - already implemented for you.
    // They start working once Questions 1 - 2 are done.
    ///////////////////////////////////////////////////////////////////////////

    /*
     * @pre: word is in lowercase
     * @pre: the method initModel was called (the language model is initialized)
     * @post: $ret = -1 if word is not in vocabulary, otherwise $ret = the index of word in vocabulary
     */
    public int getWordIndex(String word) {
        return findIndex(mVocabulary, word);
    }

    /*
     * @pre: word1, word2 are in lowercase
     * @pre: the method initModel was called (the language model is initialized)
     * @post: $ret = the count for the bigram <word1, word2>. if one of the words does not
     * exist in the vocabulary, $ret = 0
     */
    public int getBigramCount(String word1, String word2) {
        int index1 = getWordIndex(word1);
        int index2 = getWordIndex(word2);
        if (index1 == ELEMENT_NOT_FOUND || index2 == ELEMENT_NOT_FOUND) {
            return 0;
        }
        return mBigramCounts[index1][index2];
    }

    /*
     * @pre: word is in lowercase, and is in mVocabulary
     * @pre: the method initModel was called (the language model is initialized)
     * @post: $ret = the word with the lowest vocabulary index that appears most frequently after word
     * (if a bigram starting with word was never seen, $ret will be null)
     */
    public String getMostFrequentProceeding(String word) {
        int[] row = mBigramCounts[getWordIndex(word)];
        int best = ELEMENT_NOT_FOUND;
        for (int i = 0; i < row.length; i++) {
            if (row[i] > 0 && (best == ELEMENT_NOT_FOUND || row[i] > row[best])) {
                best = i;
            }
        }
        return best == ELEMENT_NOT_FOUND ? null : mVocabulary[best];
    }

    /*
     * @pre: arr1.length = arr2.length
     * @post: if arr1 or arr2 are only filled with zeros, $ret = -1, otherwise calculates CosineSim
     */
    public static double calcCosineSim(int[] arr1, int[] arr2) {
        double dot = 0;
        double norm1 = 0;
        double norm2 = 0;
        for (int i = 0; i < arr1.length; i++) {
            dot += (double) arr1[i] * arr2[i];
            norm1 += (double) arr1[i] * arr1[i];
            norm2 += (double) arr2[i] * arr2[i];
        }
        if (norm1 == 0 || norm2 == 0) {
            return -1;
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /*
     * @pre: word is in vocabulary
     * @pre: the method initModel was called (the language model is initialized)
     * @post: $ret = w implies that w is the word with the largest cosineSimilarity(vector for word, vector for w)
     * among all the other words in vocabulary (the lowest index wins a tie)
     */
    public String getClosestWord(String word) {
        int wordIndex = getWordIndex(word);
        int best = ELEMENT_NOT_FOUND;
        double bestSim = 0;
        for (int i = 0; i < mVocabulary.length; i++) {
            if (i == wordIndex) {
                continue;
            }
            double sim = calcCosineSim(mBigramCounts[wordIndex], mBigramCounts[i]);
            if (best == ELEMENT_NOT_FOUND || sim > bestSim) {
                best = i;
                bestSim = sim;
            }
        }
        // A vocabulary with a single word: the closest word is the word itself
        return best == ELEMENT_NOT_FOUND ? word : mVocabulary[best];
    }
}
