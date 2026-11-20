package il.ac.tau.cs.software1.bigram;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
        BufferedReader reader = new BufferedReader(new FileReader(fileName)); // build reader
        StringBuilder legal_words = new StringBuilder(""); // mutable string
        String line;
        int count = 0;
        while ((count < MAX_VOCABULARY_SIZE) && ((line = reader.readLine()) != null)) { // reads line-line
            String trimmed = line.trim(); // ignors edge spaces
            if (trimmed.isEmpty()) {
                continue; // skip on empty lines
            }
            String[] words_to_read = trimmed.split("\\s+"); // split the line to words
            for(String word : words_to_read) { 
                if (count >= MAX_VOCABULARY_SIZE){
                    break;
                }
                String normalized_word = normalizeWord(word);
                if(normalized_word != null) { // checks the word is legal
                    if(legal_words.indexOf(" " + normalized_word + " ") == -1) { // checks if the word NOT exist in vocabulary 
                        legal_words.append(" " + normalized_word + " ");
                        count++;
                    }
                }
            }
        }
        reader.close();
        if (count == 0) {
            return new String[0];
        }
        String[] voc = legal_words.toString().trim().split("\\s+"); 
        return voc;
    }

    /*
     * @post: mVocabulary = prev(mVocabulary)
     * @post: mBigramCounts = prev(mBigramCounts)
     */
    public int[][] buildCountsArray(String fileName, String[] vocabulary) throws IOException { // Question 2
        int n = vocabulary.length;
        int[][] counts_matrix = new int[n][n]; // creates matrix, cells initialized to 0
        BufferedReader reader = new BufferedReader(new FileReader(fileName)); // build reader
        String line;
        while ((line = reader.readLine()) != null) { // reads line-line
            String trimmed = line.trim(); // ignors edge spaces
            if (trimmed.isEmpty()) {
                continue; // skip on empty lines
            }
            String[] words_to_read = trimmed.split("\\s+"); // split the line to words
            for(int i=0; i < words_to_read.length-1; i++) {
                String word_x = normalizeWord(words_to_read[i]);
                String word_y = normalizeWord(words_to_read[i+1]);
                int x = findIndex(vocabulary, word_x);
                int y = findIndex(vocabulary, word_y);
                if( (x == ELEMENT_NOT_FOUND) || (y == ELEMENT_NOT_FOUND)) {
                    continue;
                }
                counts_matrix[x][y]++;
            }
        }
        reader.close();
        return counts_matrix;
    }

    /*
     * @pre: the method initModel was called (the language model is initialized)
     * @pre: fileName is a legal file path
     */
    public void saveModel(String fileName) throws IOException { // Question 3
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName + VOC_FILE_SUFFIX));
        int voc_len = this.mVocabulary.length;
        bufferedWriter.write(voc_len+ " words" + System.lineSeparator());
        for(int i = 0; i< voc_len; i++) {
            bufferedWriter.write(i+","+mVocabulary[i] + System.lineSeparator());
        }
        bufferedWriter.close();
        BufferedWriter bufferedWriter_counts = new BufferedWriter(new FileWriter(fileName + COUNTS_FILE_SUFFIX));
        for(int x = 0; x<voc_len; x++) {
            for(int y = 0; y< voc_len; y++) {
                int count_appears = this.mBigramCounts[x][y];
                if (count_appears == 0) {
                    continue;
                }
                bufferedWriter_counts.write(x+","+y+":"+count_appears+ System.lineSeparator());
            }
        }
        bufferedWriter_counts.close();
    }

    /*
     * @pre: fileName is a legal file path
     */
    public void loadModel(String fileName) throws IOException { // Question 4
        BufferedReader reader_voc = new BufferedReader(new FileReader(fileName + VOC_FILE_SUFFIX)); // build reader
        String line = reader_voc.readLine();
        String[] voc_describe_arr = line.trim().split("\\s+");
        int voc_len = Integer.parseInt(voc_describe_arr[0]); // mVocabulary length
        this.mVocabulary = new String[voc_len];
        while ((line = reader_voc.readLine()) != null) { // reads line-line
            String[] voc_word = line.trim().split(",");
            int i = Integer.parseInt(voc_word[0]); // place in mVocabulary
            String word = voc_word[1]; // word to write
            this.mVocabulary[i] = word;
        }
        reader_voc.close();
        this.mBigramCounts = new int[voc_len][voc_len]; // create matrix
        BufferedReader reader_count = new BufferedReader(new FileReader(fileName + COUNTS_FILE_SUFFIX)); // build reader
        String line_count;
        while ((line_count = reader_count.readLine()) != null) { // reads line-line
            String[] get_couple_appears = line_count.trim().split(":");
            int appears = Integer.parseInt(get_couple_appears[1]);
            String[] get_x_y = get_couple_appears[0].trim().split(",");
            int x = Integer.parseInt(get_x_y[0]);
            int y = Integer.parseInt(get_x_y[1]);
            this.mBigramCounts[x][y] = appears;
        }
        reader_count.close();
    }

    /*
     * @pre: sentence is in lowercase
     * @pre: the method initModel was called (the language model is initialized)
     * @pre: each two words in the sentence are separated with a single space
     * @post: if sentence is probable, according to the model, $ret = true, else, $ret = false
     */
    public boolean isLegalSentence(String sentence) { // Question 5
        if (sentence.length() == 0) { // empty string
            return true;
        }
        String[] words = sentence.trim().split("\\s+");
        int n = words.length;
        if (n==1) { // one word
            int index_word = findIndex(this.mVocabulary, words[0]);
            return (index_word != ELEMENT_NOT_FOUND);
        }
        for(int i=0; i<n-1;i++) {
            String word_x = words[i];
            String word_y = words[i+1];
            int x = findIndex(this.mVocabulary, word_x);
            int y = findIndex(this.mVocabulary, word_y);
            if (x == ELEMENT_NOT_FOUND || y == ELEMENT_NOT_FOUND) {
                return false;
            }
            if (this.mBigramCounts[x][y]==0) {
                return false;
            }
        }
        return true;
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
