extends RefCounted

var maximum: int = 5
var answer: int = 0
var solved: bool = false
var random := RandomNumberGenerator.new()

func _init() -> void:
	random.randomize()
	next_round()

func set_maximum(value: int) -> bool:
	if value not in [3, 5, 10]:
		return false
	maximum = value
	next_round()
	return true

func choose(number: int) -> bool:
	if solved or number != answer:
		return false
	solved = true
	return true

func next_round() -> void:
	var next := random.randi_range(1, maximum)
	if next == answer:
		next = next % maximum + 1
	answer = next
	solved = false
