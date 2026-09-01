package com.dowinn.rouletto.logic;


import com.dowinn.rouletto.model.GameDetail;
import com.dowinn.rouletto.model.GameResult;
import com.dowinn.rouletto.service.BetSpotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GameLogic {

    private static final int[][] values = new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}, {10, 11, 12}, {13, 14, 15},
            {16, 17, 18}, {19, 20, 21}, {22, 23, 24}, {25, 26, 27}, {28, 29, 30}, {31, 32, 33},
            {34, 35, 36}};

    public boolean checkResult(GameDetail gameDetail){

        List<Integer> spotNumbers =gameDetail.getBalls();
        GameResult gameDetailResult = new GameResult();
        Map<Integer,Integer> spotBallCount=new LinkedHashMap<>();
        List<Integer> results = new ArrayList<>();
        //add actual spots
        results.addAll(gameDetail.getBalls());

        List<Integer> column1 = Arrays.asList(1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34);
        List<Integer> column2 = Arrays.asList(2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35);
        List<Integer> column3 = Arrays.asList(3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36);
        List<Integer> red = Arrays.asList(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36);
        Integer green = 0;
        // color validations
        long redCount = spotNumbers.stream().filter(a -> red.contains(a)).count();
        long greenCount = spotNumbers.stream().filter(a -> a == green).count()  ;
        long blackCount = spotNumbers.stream().filter(a -> !(red.contains(a) || a == green)).count();
        if (blackCount == 3 && greenCount == 1) {
            results.add(BetSpotService.getBetSpot("BLACK_3-GREEN_1").getIndex());
        } else if (blackCount == 4) {
            results.add(BetSpotService.getBetSpot( "BLACK_4").getIndex());
        } else if (blackCount == 3 && redCount == 1) {
            results.add(BetSpotService.getBetSpot( "BLACK_3-RED_1").getIndex());
        } else if (redCount == 3 && blackCount == 1) {
            results.add(BetSpotService.getBetSpot( "RED_3-BLACK_1").getIndex());
        } else if (redCount == 4) {
            results.add(BetSpotService.getBetSpot( "RED_4").getIndex());
        } else if (redCount == 3 && greenCount == 1) {
            results.add(BetSpotService.getBetSpot( "RED_3-GREEN_1").getIndex());
        } else if (redCount==2 && blackCount ==2) {
            results.add(BetSpotService.getBetSpot( "BLACK_2-RED_2").getIndex());
        }
        //dozen validations
        Map<Integer, Integer> dozenCountMap = new HashMap<>();
        Map<Integer, Integer> eighteenCountMap = new HashMap<>();
        for (Integer spotNumber : spotNumbers) {
            if (spotNumber >= 1 && spotNumber <= 12) {
                dozenCountMap.put(1, dozenCountMap.getOrDefault(1, 0) + 1);
            } else if (spotNumber >= 13 && spotNumber <= 24) {
                dozenCountMap.put(2, dozenCountMap.getOrDefault(2, 0) + 1);
            } else if (spotNumber >= 25 && spotNumber <= 36) {
                dozenCountMap.put(3, dozenCountMap.getOrDefault(3, 0) + 1);
            }
            if (spotNumber >= 1 && spotNumber <= 18) {
                eighteenCountMap.put(1, eighteenCountMap.getOrDefault(1, 0) + 1);
            } else if (spotNumber >= 19 && spotNumber <= 36) {
                eighteenCountMap.put(2, eighteenCountMap.getOrDefault(2, 0) + 1);
            }
        }
        if (dozenCountMap.get(1) !=null && dozenCountMap.get(1) >= 2) {
            results.add(BetSpotService.getBetSpot( "DOZEN_1").getIndex());
            spotBallCount.put(BetSpotService.getBetSpot( "DOZEN_1").getIndex(),dozenCountMap.get(1));
        }
        if (dozenCountMap.get(2) !=null && dozenCountMap.get(2) >= 2  ) {
            results.add(BetSpotService.getBetSpot( "DOZEN_2").getIndex());
            spotBallCount.put(BetSpotService.getBetSpot("DOZEN_2").getIndex(),dozenCountMap.get(2));
        }
        if (dozenCountMap.get(3) !=null && dozenCountMap.get(3) >= 2) {
            results.add(BetSpotService.getBetSpot( "DOZEN_3").getIndex());
            spotBallCount.put(BetSpotService.getBetSpot("DOZEN_3").getIndex(),dozenCountMap.get(3));
        }
        //Eighteen validations
        if (eighteenCountMap.get(1)!=null && eighteenCountMap.get(1) >= 3) {
            results.add(BetSpotService.getBetSpot( "1-18").getIndex());
            spotBallCount.put(BetSpotService.getBetSpot("1-18").getIndex(),eighteenCountMap.get(1));
        }
        if (eighteenCountMap.get(2)!=null && eighteenCountMap.get(2) >= 3) {
            results.add(BetSpotService.getBetSpot( "19-36").getIndex());
            spotBallCount.put(BetSpotService.getBetSpot("19-36").getIndex(),eighteenCountMap.get(2));
        }


        List<List<Integer>> spotMap = List.of(column1, column2, column3);
        //find Straight and column
        findStraightAndColumn(spotMap, spotNumbers, results,spotBallCount);
        for (Integer spotNumber : spotNumbers) {
            //String spot = "" + spotNumber; // straight-bet
            findSplit(spotNumber, results,spotBallCount);
            findStreet(spotNumber, results,spotBallCount);
          //  gameDetailResult.setWin(spot);
        }
        findSideBet(spotNumbers, results);
        gameDetail.setGameResult(gameDetailResult);
        List<Integer> collect = results.stream().distinct().collect(Collectors.toList());
        gameDetailResult.setWinningSpot(collect);
        gameDetailResult.setSpotBallCount(spotBallCount);
        log.info("game details {}",gameDetail);
        return true;
    }

    private static void findSideBet(List<Integer> spotNumbers, List<Integer> results) {

        int sum = spotNumbers.stream().reduce(0, (a, b) -> a + b);
        boolean zero = spotNumbers.contains(0);

        if (sum == 88) {
            results.add(BetSpotService.getBetSpot( "GREAT_88").getIndex());
        } else if (sum == 77) {
            results.add(BetSpotService.getBetSpot( "LUCKY_77").getIndex());
        }

        if (sum % 10 == 8) {
            results.add(BetSpotService.getBetSpot( "REMAINDER_8").getIndex());
        } else if (sum % 10 == 7) {
            results.add(BetSpotService.getBetSpot( "REMAINDER_7").getIndex());
        }

        if (sum <= 59) {
            results.add(BetSpotService.getBetSpot( "LOW").getIndex());
        } else if (sum <= 75) {
            results.add(BetSpotService.getBetSpot( "MEDIUM").getIndex());
        } else if (sum <= 138) {
            results.add(BetSpotService.getBetSpot( "HIGH").getIndex());
        }


        if (sum <= 52) {
            results.add(BetSpotService.getBetSpot( "A").getIndex());
        } else if (sum <= 63) {
            results.add(BetSpotService.getBetSpot( "B").getIndex());
        } else if (sum <= 75) {
            results.add(BetSpotService.getBetSpot( "C").getIndex());
        } else if (sum <= 88) {
            results.add(BetSpotService.getBetSpot( "D").getIndex());
        } else if (sum <= 138) {
            results.add(BetSpotService.getBetSpot( "E").getIndex());
        }

        if (zero) {
            results.add(BetSpotService.getBetSpot( "FORTUNE_ZERO").getIndex());
        } else {
            if (sum % 2 == 0) {
                results.add(BetSpotService.getBetSpot( "EVEN_SIDEBET").getIndex());
            } else {
                results.add(BetSpotService.getBetSpot( "ODD_SIDEBET").getIndex());
            }
        }

    }


    private static void findStraightAndColumn(List<List<Integer>> spotMap, List<Integer> spotn, List<Integer> results,Map<Integer,Integer> ballCount) {

        Map<Integer, Integer> rowCount = new LinkedHashMap<>();
        ArrayList<Integer> spotNumbers=new ArrayList<>(spotn);
        Collections.sort(spotNumbers);
        for (Integer num : spotNumbers) {
            if (spotMap.get(0).contains(num)) {
                rowCount.put(0, rowCount.getOrDefault(0, 0) + 1);
            } else if (spotMap.get(1).contains(num)) {
                rowCount.put(1, rowCount.getOrDefault(1, 0) + 1);
            } else if (spotMap.get(2).contains(num)) {
                rowCount.put(2, rowCount.getOrDefault(2, 0) + 1);
            }
        }
        //find column
        List<Integer> matchColumn = rowCount.entrySet().stream().filter(a -> a.getValue() >= 2).map(a -> a.getKey()).collect(Collectors.toList());
        if (matchColumn.contains(0)) {
            results.add(BetSpotService.getBetSpot( "COLUMN_1").getIndex());
            ballCount.put(BetSpotService.getBetSpot( "COLUMN_1").getIndex(),rowCount.get(0));
            //column1
        }
        if (matchColumn.contains(1)) {
            results.add(BetSpotService.getBetSpot( "COLUMN_2").getIndex());
            ballCount.put(BetSpotService.getBetSpot("COLUMN_2").getIndex(),rowCount.get(1));
            //column2
        }
        if (matchColumn.contains(2)) {
            results.add(BetSpotService.getBetSpot( "COLUMN_3").getIndex());
            ballCount.put(BetSpotService.getBetSpot("COLUMN_3").getIndex(),rowCount.get(2));
            //column3
        }
/*

        Optional<Integer> opt = rowCount.entrySet().stream().filter(a -> a.getValue() >= 3).map(a -> a.getKey()).findFirst();
        if (opt.isEmpty()) return;
*/

/*
        Integer rowId = opt.get();
*//*
        List<Integer> row = spotMap.get(rowId);*/
 /*       List<Integer> values = spotNumbers.stream().sorted().toList();
        Integer sequenceCount = 0;
        Integer i1 = values.get(0);
        int rowStartIndex = row.indexOf(i1);
        for (int i = 0; i < values.size(); i++) {
            if (row.get(rowStartIndex + i) == values.get(i)) {
                sequenceCount++;
            } else {
                if (i == 1) {
                    sequenceCount = 1;
                    rowStartIndex = rowStartIndex + 1;
                } else {
                    break;
                }
            }*/
        List<Integer> values = spotNumbers.stream().sorted().toList();
        int sequenceCount = 1;
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i) + 1 == values.get(i + 1)) {
                sequenceCount++;
                if (sequenceCount == 4) {
                    break;
                }
            } else {
                if(sequenceCount==3) {
                    break;
                }
                else {sequenceCount = 1;}
            }
        }
        log.info("sequnce count {}",sequenceCount);
        if (sequenceCount == 4) {
            results.add(BetSpotService.getBetSpot("STRAIGHT_4").getIndex());
            results.add(BetSpotService.getBetSpot("STRAIGHT_3").getIndex());
        } else if (sequenceCount == 3) {
            results.add(BetSpotService.getBetSpot("STRAIGHT_3").getIndex());
        }

    }

    private static void findLine(int spotNumber, List<Integer> results) {
        int row = (spotNumber - 1) / 3;

        if (spotNumber > 0) {
            if (row > 0 && row <= 11) {
                results.add(BetSpotService.getBetSpot( "LINE_" + values[row - 1][0] + "_to_" + values[row][2]).getIndex());
            }

            if (row >= 0 && row < 11) {
                results.add(BetSpotService.getBetSpot( "LINE_" + values[row][0] + "_to_" + values[row + 1][2]).getIndex());
            }
        }
    }

    private static void findStreet(int spotNumber, List<Integer> results,Map<Integer,Integer> spotBallCount) {

        int row = (spotNumber - 1) / 3;
        int col = (spotNumber - 1) % 3;

        int leftCol = col - 1;
        int rightCol = col + 1;

        if (col == 0) {
            results.add(BetSpotService.getBetSpot( "STREET_" + values[row][col] + "_to_" + values[row][rightCol + 1])
                    .getIndex());
            spotBallCount.put(BetSpotService.getBetSpot( "STREET_" + values[row][col] + "_to_" + values[row][rightCol + 1]).getIndex(),spotBallCount.getOrDefault(BetSpotService.getBetSpot( "STREET_" + values[row][col] + "_to_" + values[row][rightCol + 1]).getIndex(),0)+1);

        }
        if (col == 1) {
            results.add(BetSpotService.getBetSpot( "STREET_" + values[row][leftCol] + "_to_" + values[row][rightCol])
                    .getIndex());
            spotBallCount.put(BetSpotService.getBetSpot( "STREET_" + values[row][leftCol] + "_to_" + values[row][rightCol]).getIndex(),spotBallCount.getOrDefault(BetSpotService.getBetSpot( "STREET_" + values[row][leftCol] + "_to_" + values[row][rightCol]).getIndex(),0)+1);

        }
        if (col == 2) {
            results.add(BetSpotService.getBetSpot( "STREET_" + values[row][leftCol - 1] + "_to_" + values[row][col])
                    .getIndex());
            spotBallCount.put(BetSpotService.getBetSpot( "STREET_" + values[row][leftCol - 1] + "_to_" + values[row][col]).getIndex(),spotBallCount.getOrDefault(BetSpotService.getBetSpot( "STREET_" + values[row][leftCol - 1] + "_to_" + values[row][col]).getIndex(),0)+1);

        }

        if (spotNumber == 0 || spotNumber == 1 || spotNumber == 2) {
        //todo::    results.add(BetSpotService.getBetSpot( "ZERO_STREET_0_1_2").getIndex());
        }

        if (spotNumber == 0 || spotNumber == 2 || spotNumber == 3) {
        //todo::    results.add(BetSpotService.getBetSpot( "ZERO_STREET_0_2_3").getIndex());
        }
    }

    private static void findSquare(int spotNumber, List<Integer> results) {

        int row = (spotNumber - 1) / 3;
        int col = (spotNumber - 1) % 3;

        int topRow = row - 1;
        int bottomRow = row + 1;

        int leftCol = col - 1;
        int rightCol = col + 1;

        if (spotNumber >= 0 && spotNumber <= 3) {
            results.add(BetSpotService.getBetSpot( "SQUARE_0_1_2_3").getIndex());
        }
        if (col >= 0) {
            if (topRow >= 0 && leftCol >= 0) {
                results.add(BetSpotService.getBetSpot( "SQUARE_" + values[topRow][leftCol] + "_" + values[topRow][col] + "_"
                        + values[row][leftCol] + "_" + values[row][col]).getIndex());
            }
            if (topRow >= 0 && rightCol <= 2) {
                results.add(BetSpotService.getBetSpot( "SQUARE_" + values[topRow][col] + "_" + values[topRow][rightCol]
                        + "_" + values[row][col] + "_" + values[row][rightCol]).getIndex());
            }
            if (leftCol >= 0 && bottomRow <= 11) {
                results.add(BetSpotService.getBetSpot( "SQUARE_" + values[row][leftCol] + "_" + values[row][col] + "_"
                        + values[bottomRow][leftCol] + "_" + values[bottomRow][col]).getIndex());
            }
            if (bottomRow <= 11 && rightCol <= 2) {
                results.add(BetSpotService.getBetSpot( "SQUARE_" + values[row][col] + "_" + values[row][rightCol] + "_"
                        + values[bottomRow][col] + "_" + values[bottomRow][rightCol]).getIndex());
            }
        }
    }

    private static void  findSplit(int spotNumber, List<Integer> results,Map<Integer,Integer> spotBallCount) {

        int row = (spotNumber - 1) / 3;
        int col = (spotNumber - 1) % 3;
        int topRow = row - 1;
        int bottomRow = row + 1;

        int leftCol = col - 1;
        int rightCol = col + 1;
        if (spotNumber == 0 || spotNumber == 1) {
            results.add(BetSpotService.getBetSpot( "SPLIT_0_1").getIndex());
            spotBallCount.put(BetSpotService.getBetSpot( "SPLIT_0_1").getIndex(),spotBallCount.getOrDefault(BetSpotService.getBetSpot( "SPLIT_0_1").getIndex(),0)+1);
        }
        if (spotNumber == 0 || spotNumber == 2) {
            results.add(BetSpotService.getBetSpot( "SPLIT_0_2").getIndex());
            spotBallCount.put(BetSpotService.getBetSpot( "SPLIT_0_2").getIndex(),spotBallCount.getOrDefault(BetSpotService.getBetSpot( "SPLIT_0_2").getIndex(),0)+1);
        }
        if (spotNumber == 0 || spotNumber == 3) {
            results.add(BetSpotService.getBetSpot( "SPLIT_0_3").getIndex());
            spotBallCount.put(BetSpotService.getBetSpot( "SPLIT_0_3").getIndex(),spotBallCount.getOrDefault(BetSpotService.getBetSpot( "SPLIT_0_3").getIndex(),0)+1);
        }

        if (col >= 0) {
            if (topRow >= 0) {
                results.add(BetSpotService.getBetSpot( "SPLIT_" + values[topRow][col] + "_" + spotNumber).getIndex());
                spotBallCount.put(BetSpotService.getBetSpot(  "SPLIT_" + values[topRow][col] + "_" + spotNumber).getIndex(),spotBallCount.getOrDefault(BetSpotService.getBetSpot(  "SPLIT_" + values[topRow][col] + "_" + spotNumber).getIndex(),0)+1);
            }

            if (leftCol >= 0) {
                results.add(BetSpotService.getBetSpot( "SPLIT_" + values[row][leftCol] + "_" + spotNumber).getIndex());
                spotBallCount.put(BetSpotService.getBetSpot(  "SPLIT_" + values[row][leftCol] + "_" + spotNumber).getIndex(),spotBallCount.getOrDefault(BetSpotService.getBetSpot(  "SPLIT_" + values[row][leftCol] + "_" + spotNumber).getIndex(),0)+1);
            }

            if (rightCol <= 2) {
                results.add(BetSpotService.getBetSpot( "SPLIT_" + spotNumber + "_" + values[row][rightCol]).getIndex());
                spotBallCount.put(BetSpotService.getBetSpot(  "SPLIT_" + spotNumber + "_" + values[row][rightCol]).getIndex(),spotBallCount.getOrDefault(BetSpotService.getBetSpot(  "SPLIT_" + spotNumber + "_" + values[row][rightCol]).getIndex(),0)+1);

            }

            if (bottomRow < values.length) {
                results.add(BetSpotService.getBetSpot( "SPLIT_" + spotNumber + "_" + values[bottomRow][col]).getIndex());
                spotBallCount.put(BetSpotService.getBetSpot(  "SPLIT_" + spotNumber + "_" + values[bottomRow][col]).getIndex(),spotBallCount.getOrDefault(BetSpotService.getBetSpot(  "SPLIT_" + spotNumber + "_" + values[bottomRow][col]).getIndex(),0)+1);
            }
        }
    }
}
