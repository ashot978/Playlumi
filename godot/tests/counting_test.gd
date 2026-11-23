extends SceneTree

const CountingGame = preload("res://counting_game.gd")

func _initialize() -> void:
	var game = CountingGame.new()
	if not game.set_maximum(3) or game.maximum != 3:
		_fail("maximum 3")
		return
	if game.set_maximum(4) or game.maximum != 3:
		_fail("invalid maximum")
		return
	for i in 300:
		var previous: int = game.answer
		if previous < 1 or previous > game.maximum:
			_fail("range")
			return
		var wrong: int = previous % game.maximum + 1
		if game.choose(wrong) or game.solved:
			_fail("wrong answer")
			return
		if not game.choose(previous) or not game.solved or game.choose(previous):
			_fail("correct answer")
			return
		game.next_round()
		if game.answer == previous or game.solved:
			_fail("next round")
			return
	for maximum in [5, 10]:
		if not game.set_maximum(maximum) or game.maximum != maximum:
			_fail("supported maximum")
			return
	print("Counting tests passed")
	quit(0)

func _fail(reason: String) -> void:
	printerr("Counting test failed: " + reason)
	quit(1)
