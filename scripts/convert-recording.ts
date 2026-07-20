#!/usr/bin/env bun
// Converts a third-party movement recording (e.g. an angle-solver export like `e115.json`)
// into a parkour-display macro file: a JSON array of `[w, a, s, d, jump, sprint, sneak, yaw,
// pitch]` tuples, matching MacroTickStateTypeAdapter, ready to drop into the addon's `macros/`
// folder. Mirrors core/src/main/java/.../macro/RecordingConverter.java.
//
// Usage: bun scripts/convert-recording.ts <input.json> [output.json]
// Without an output path, the macro JSON is printed to stdout.

type MacroTickState = [
  w: boolean,
  a: boolean,
  s: boolean,
  d: boolean,
  jump: boolean,
  sprint: boolean,
  sneak: boolean,
  yaw: number,
  pitch: number,
];

interface Recording {
  start: { yaw: number; pitch: number };
  rows: { keys: string[]; yaw?: number; pitch?: number }[];
}

function convert(recording: Recording): MacroTickState[] {
  let yaw = recording.start.yaw;
  let pitch = recording.start.pitch;

  return recording.rows.map((row) => {
    if (row.yaw !== undefined) {
      yaw = row.yaw;
    }
    if (row.pitch !== undefined) {
      pitch = row.pitch;
    }

    const keys = new Set(row.keys.map((key) => key.toUpperCase()));

    return [
      keys.has("W"),
      keys.has("A"),
      keys.has("S"),
      keys.has("D"),
      keys.has("JUMP"),
      keys.has("SPRINT"),
      keys.has("SNEAK"),
      yaw,
      pitch,
    ];
  });
}

const [inputPath, outputPath] = process.argv.slice(2);

if (!inputPath) {
  console.error("Usage: bun scripts/convert-recording.ts <input.json> [output.json]");
  process.exit(1);
}

const recording: Recording = await Bun.file(inputPath).json();
const macro = JSON.stringify(convert(recording), null, 2);

if (outputPath) {
  await Bun.write(outputPath, macro + "\n");
} else {
  console.log(macro);
}
