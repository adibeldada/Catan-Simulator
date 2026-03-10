import json
from typing import Dict, Optional, Tuple, List
import numpy as np

from catanatron.models.map import (
    CatanMap,
    LandTile,
    initialize_tiles,
    MapTemplate
)
from catanatron.models.board import Board, STATIC_GRAPH
from catanatron.models.player import Color, Player
from catanatron.models.enums import WOOD, BRICK, SHEEP, WHEAT, ORE, SETTLEMENT, CITY
from catanatron.game import Game
from catanatron.state import State
from catanatron.gym.envs.pygame_renderer import PygameRenderer
from PIL import Image
import sys
import os
import time


class CatanBoardVisualizer:

    def __init__(self):
        self.map_data: Optional[Dict] = None
        self.state_data: Optional[Dict] = None
        self.game: Optional[Game] = None

    def load_map_json(self, json_path: str) -> None:
        with open(json_path, 'r') as f:
            self.map_data = json.load(f)

    def load_state_json(self, json_path: str) -> None:
        with open(json_path, 'r') as f:
            self.state_data = json.load(f)

    def _parse_resource(self, resource_str: Optional[str]) -> Optional[str]:
        if resource_str is None or resource_str == "DESERT":
            return None
        resource_map = {
            "WOOD": WOOD,
            "BRICK": BRICK,
            "SHEEP": SHEEP,
            "WHEAT": WHEAT,
            "ORE": ORE,
        }
        if resource_str not in resource_map:
            raise ValueError(f"Unknown resource: {resource_str}")
        return resource_map[resource_str]

    def _parse_color(self, color_str: str) -> Color:
        color_map = {
            "RED": Color.RED,
            "BLUE": Color.BLUE,
            "ORANGE": Color.ORANGE,
            "WHITE": Color.WHITE,
        }
        if color_str not in color_map:
            raise ValueError(f"Unknown color: {color_str}")
        return color_map[color_str]

    def _create_map_from_json(self) -> CatanMap:
        if self.map_data is None:
            raise ValueError("No map data loaded. Call load_map_json first.")

        tile_coords = []
        resources = []
        numbers = []

        for tile_data in self.map_data["tiles"]:
            coord = (tile_data["q"], tile_data["s"], tile_data["r"])
            if sum(coord) != 0:
                raise ValueError(f"Invalid cube coordinate: {coord}. Sum must be 0.")
            tile_coords.append(coord)
            resource = self._parse_resource(tile_data["resource"])
            resources.append(resource)
            if resource is not None:
                numbers.append(tile_data["number"])

        topology = {coord: LandTile for coord in tile_coords}
        template = MapTemplate(
            numbers=numbers,
            port_resources=[],
            tile_resources=resources,
            topology=topology,
        )

        tiles = initialize_tiles(
            template,
            shuffled_numbers_param=list(reversed(numbers)),
            shuffled_port_resources_param=[],
            shuffled_tile_resources_param=list(reversed(resources)),
        )

        return CatanMap.from_tiles(tiles)

    def _apply_state_to_board(self, board: Board):
        """Apply buildings and roads, bypassing all validation."""

        # --- Buildings first ---
        for building_data in self.state_data.get("buildings", []):
            node_id = building_data["node"]
            color = self._parse_color(building_data["owner"])
            building_type = building_data["type"]

            try:
                if building_type == "CITY":
                    board.buildings[node_id] = (color, CITY)
                else:
                    board.buildings[node_id] = (color, SETTLEMENT)

                # Register in connected components so roads can attach
                board.connected_components[color].append({node_id})

                # Enforce distance rule on remaining buildable spots
                board.board_buildable_ids.discard(node_id)
                for neighbor in STATIC_GRAPH.neighbors(node_id):
                    board.board_buildable_ids.discard(neighbor)
            except Exception:
                pass  # skip any problematic building silently

        # --- Roads second ---
        for road_data in self.state_data.get("roads", []):
            edge = (road_data["a"], road_data["b"])
            color = self._parse_color(road_data["owner"])
            try:
                board.build_road(color, edge)
            except Exception:
                pass  # skip invalid roads silently

    def build_game(self) -> Game:
        catan_map = self._create_map_from_json()

        players = [
            Player(Color.RED),
            Player(Color.BLUE),
            Player(Color.WHITE),
            Player(Color.ORANGE),
        ]

        game = Game(
            players=players,
            seed=42,
            catan_map=catan_map,
            initialize=True,
        )

        self._apply_state_to_board(game.state.board)

        self.game = game
        return game

    def render(
            self,
            output_dir: Optional[str] = None,
            render_scale: float = 1.0,
            show: bool = False,
    ) -> np.ndarray:
        if self.game is None:
            self.build_game()

        renderer = PygameRenderer(render_scale=render_scale)
        rgb_array = renderer.render(self.game)

        os.makedirs(output_dir, exist_ok=True)
        file_count = len(
            [f for f in os.listdir(output_dir) if os.path.isfile(os.path.join(output_dir, f))]
        )
        output_path = os.path.join(output_dir, f"board{file_count}.png")

        img = Image.fromarray(rgb_array)
        img.save(output_path)
        print(f"Board rendered and saved to {output_path}")

        renderer.close()
        return output_path


def visualize_board_from_json(
        map_json_path: str,
        state_json_path: str,
        output_dir: str = "scraped_boards",
        render_scale: float = 1.0,
) -> None:
    visualizer = CatanBoardVisualizer()
    visualizer.load_map_json(map_json_path)
    visualizer.load_state_json(state_json_path)
    visualizer.render(output_dir=output_dir, render_scale=render_scale)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage:")
        print("  python light_visualizer.py base_map.json state.json")
        print("  python light_visualizer.py base_map.json --watch")
        sys.exit(1)

    base_map_path = sys.argv[1]
    watch_mode = "--watch" in sys.argv
    state_path = "state.json"

    if len(sys.argv) >= 3:
        if sys.argv[2] != "--watch" and sys.argv[2].endswith(".json"):
            state_path = sys.argv[2]

    last_mtime = None
    print("Visualizer started.")
    if watch_mode:
        print("Watch mode enabled. Waiting for state.json changes...")

    while True:
        if os.path.exists(state_path):
            mtime = os.path.getmtime(state_path)
            if (not watch_mode) or (mtime != last_mtime):
                last_mtime = mtime
                visualize_board_from_json(base_map_path, state_path)
        if not watch_mode:
            break
        time.sleep(0.5)