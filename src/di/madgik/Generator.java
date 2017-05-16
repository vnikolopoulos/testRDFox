package di.madgik;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by vaggelis on 9/5/2017.
 */
public class Generator {
    public void generateV2(String outFile, double selectivity, int rows_per_table) {
        System.out.println("Filename: "+ outFile + " Selectivity: " + selectivity + " Rows: " + rows_per_table);
        System.out.println("Generating v2...");
        long cp_size = (long) rows_per_table * rows_per_table;
        long expected_join_results = (long) (cp_size * selectivity);
        double select_prob = (double) expected_join_results/rows_per_table;
        System.out.println("Cartesian product size: " + cp_size + " expected join results: " + expected_join_results);
        System.out.println("Select prob: " + select_prob);
        System.out.println();

        List<Integer> input = new ArrayList<>();
        ThreadLocalRandom.current().ints(0, Integer.MAX_VALUE).distinct().limit(rows_per_table).forEach(value -> input.add(value));

        List<Integer> r1x = new ArrayList<>();
        List<Integer> r2x = new ArrayList<>();
        PopulateArray(rows_per_table, input, r1x, select_prob);
        PopulateArray(rows_per_table, input, r2x, select_prob);
//        System.out.println("R1X: " + r1x);
//        System.out.println("R2X: " + r2x);

        input.clear();
        ThreadLocalRandom.current().ints(0, Integer.MAX_VALUE).distinct().limit(rows_per_table*10).forEach(value -> input.add(value));
        List<Integer> r2z = new ArrayList<>();
        List<Integer> r3z = new ArrayList<>();
        PopulateArray(rows_per_table, input, r2z, select_prob);
        PopulateArray(rows_per_table, input, r3z, select_prob);

        List<Integer> r1y = new ArrayList<>();
        ThreadLocalRandom.current().ints(0, Integer.MAX_VALUE).limit(rows_per_table).forEach(value->r1y.add(value));
        List<Integer> r3w = new ArrayList<>();
        ThreadLocalRandom.current().ints(0, Integer.MAX_VALUE).limit(rows_per_table).forEach(value->r3w.add(value));
        //System.out.println("R1X: " + r1x);
        //System.out.println("R2X: " + r2x);

        Collections.shuffle(r1x);
        Collections.shuffle(r1y);
        Collections.shuffle(r2x);
        Collections.shuffle(r2z);
        Collections.shuffle(r3z);
        Collections.shuffle(r3w);

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFile), "utf-8"))) {

            //Print R1
            for (int i  = 0 ; i < rows_per_table ; ++i){
                writer.write("<"+r1x.get(i)+"> <r1> <"+r1y.get(i)+"> .\n");
            }
            //Print R2
            for (int i  = 0 ; i < rows_per_table ; ++i){
                writer.write("<"+r2x.get(i)+"> <r2> <"+r2z.get(i)+"> .\n");
            }
            //Print R3
            for (int i  = 0 ; i < rows_per_table ; ++i){
                writer.write("<"+r3z.get(i)+"> <r3> <"+r3w.get(i)+"> .\n");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        int join_res =0;
//        for (int r1xv : r1x){
//            for (int r2xv : r2x){
//                if (r1xv == r2xv){
//                    join_res++;
//                }
//            }
//        }
//        System.out.println("Join results: " + join_res);
        System.out.println("Done");
    }

    private void PopulateArray(int rows_per_table, List<Integer> input, List<Integer> r1x, double select_prob) {
        int in_idx = 0;
        Random rng = new Random();
        while (r1x.size() < rows_per_table){
            double cur_prob  = select_prob;
            while (r1x.size() < rows_per_table && cur_prob > 0) {
                if (cur_prob >= 1) {
                    r1x.add(input.get(in_idx%input.size()));
                } else if (rng.nextDouble() < cur_prob) {
                    r1x.add((input.get(in_idx%input.size())));
                }
                cur_prob--;
            }
            in_idx++;
        }
    }

    public void generateDistinct(String outFile, double selectivity, int rows_per_table) {
        System.out.println("Filename: "+ outFile + " Selectivity: " + selectivity + " Rows: " + rows_per_table);
        System.out.println("Generating...");
        int selectivity_percent = (int) (selectivity * 100);
        //Generate for table R1
        Set<Integer> r1x = new HashSet<>();
        Set<Integer> r1y = new HashSet<>();
        //Populate R1 with distinct random X
        ThreadLocalRandom.current().ints(0, Integer.MAX_VALUE).distinct().limit(rows_per_table).forEach(value -> r1x.add(value));
        //Populate R1 with distinct random Y
        ThreadLocalRandom.current().ints(0, Integer.MAX_VALUE).distinct().limit(rows_per_table).forEach(value -> r1y.add(value));
       // System.out.println("R1x: " + r1x.toString());
       // System.out.println("R1y: " + r1y.toString());
        //Generate for table R2
        Set<Integer> r2x = new HashSet<>();
        Set<Integer> r2z = new HashSet<>();
        //Populate R2 with (selectivity_percent * rows_per_table) / 100 X from R1
        r1x.stream().limit( (selectivity_percent * rows_per_table) / 100 ).forEach(v -> r2x.add(v));
        Random rng = new Random();
        //Fill R2 to reach desirable size
        while (r2x.size() < rows_per_table){
            r2x.add(Math.abs(rng.nextInt()));
        }
        //Populate R1 with distinct random Z
        ThreadLocalRandom.current().ints(0, Integer.MAX_VALUE).distinct().limit(rows_per_table).forEach(value -> r2z.add(value));
       // System.out.println("R2x: " + r2x.toString());
       // System.out.println("R2z: " + r2z.toString());

        //Generate for table R3
        Set<Integer> r3z = new HashSet<>();
        Set<Integer> r3w = new HashSet<>();
        //Populate R3 with (selectivity_percent * rows_per_table) / 100 Z from R2
        r2z.stream().limit( (selectivity_percent * rows_per_table) / 100 ).forEach(v -> r3z.add(v));
        //Fill R3 to reach desirable size
        while (r3z.size() < rows_per_table){
            r3z.add(Math.abs(rng.nextInt()));
        }
        //Populate R3 with distinct random W
        ThreadLocalRandom.current().ints(0, Integer.MAX_VALUE).distinct().limit(rows_per_table).forEach(value -> r3w.add(value));
      //  System.out.println("R3z: " + r3z.toString());
     //   System.out.println("R3w: " + r3w.toString());

        ArrayList r1x_rnd = new ArrayList(r1x);
        ArrayList r1y_rnd = new ArrayList(r1y);
        ArrayList r2x_rnd = new ArrayList(r2x);
        ArrayList r2z_rnd = new ArrayList(r2z);
        ArrayList r3z_rnd = new ArrayList(r3z);
        ArrayList r3w_rnd = new ArrayList(r3w);
        Collections.shuffle(r1x_rnd);
        Collections.shuffle(r1y_rnd);
        Collections.shuffle(r2x_rnd);
        Collections.shuffle(r2z_rnd);
        Collections.shuffle(r3z_rnd);
        Collections.shuffle(r3w_rnd);


        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFile), "utf-8"))) {

            //Print R1
            for (int i  = 0 ; i < rows_per_table ; ++i){
                writer.write("<"+r1x_rnd.get(i)+"> <r1> <"+r1y_rnd.get(i)+"> .\n");
            }
            //Print R2
            for (int i  = 0 ; i < rows_per_table ; ++i){
                writer.write("<"+r2x_rnd.get(i)+"> <r2> <"+r2z_rnd.get(i)+"> .\n");
            }
            //Print R3
            for (int i  = 0 ; i < rows_per_table ; ++i){
                writer.write("<"+r3z_rnd.get(i)+"> <r3> <"+r3w_rnd.get(i)+"> .\n");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done!");
    }

}
