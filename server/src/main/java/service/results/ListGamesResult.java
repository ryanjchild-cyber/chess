package service.results;
import service.GameSummary;

import java.util.List;

public record ListGamesResult(List<GameSummary> games) {}
