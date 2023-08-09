SRC_CORE=som/Error.cpp						\
			som/Random.cpp						\
			som/Dictionary.cpp					\
			som/IdentityDictionary.cpp			\
			som/Vector.cpp						\
			Bounce.cpp							\
			Run.cpp 							\
			Harness.cpp							\
			List.cpp							\
			Mandelbrot.cpp						\
			Benchmark.cpp						\
			Sieve.cpp							\
			Permute.cpp							\
			Queens.cpp							\
			nbody/Body.cpp						\
			nbody/NBodySystem.cpp				\
			NBody.cpp							\
			json/JsonArray.cpp					\
			json/JsonLiteral.cpp				\
			json/JsonNumber.cpp					\
			json/JsonObject.cpp					\
			json/JsonPureStringParser.cpp		\
			json/JsonString.cpp					\
			json/JsonValue.cpp					\
			json/ParseException.cpp				\
			Json.cpp							\
			havlak/BasicBlock.cpp				\
			havlak/BasicBlockEdge.cpp			\
			havlak/ControlFlowGraph.cpp			\
			havlak/HavlakLoopFinder.cpp			\
			havlak/LoopTesterApp.cpp			\
			havlak/SimpleLoop.cpp				\
			havlak/LoopStructureGraph.cpp		\
			havlak/UnionFindNode.cpp			\
			Havlak.cpp							\
			deltablue/Strength.cpp				\
			deltablue/ScaleConstraint.cpp		\
			deltablue/AbstractConstraint.cpp	\
			deltablue/BinaryConstraint.cpp		\
			deltablue/EditConstraint.cpp		\
			deltablue/EqualityConstraint.cpp	\
			deltablue/Plan.cpp					\
			deltablue/Planner.cpp				\
			deltablue/StayConstraint.cpp		\
			deltablue/UnaryConstraint.cpp		\
			deltablue/Variable.cpp				\
			richards/DeviceTaskDataRecord.cpp	\
			richards/HandlerTaskDataRecord.cpp	\
			richards/IdleTaskDataRecord.cpp		\
			richards/Packet.cpp					\
			richards/RBObject.cpp				\
			richards/Scheduler.cpp				\
			richards/TaskControlBlock.cpp		\
			richards/TaskState.cpp				\
			richards/WorkerTaskDataRecord.cpp	\
			Richards.cpp						\
			cd/Aircraft.cpp						\
			cd/CallSign.cpp						\
			cd/Collision.cpp					\
			cd/CollisionDetector.cpp			\
			cd/Motion.cpp						\
			cd/RedBlackTree.cpp					\
			cd/Simulator.cpp					\
			cd/Vector2D.cpp						\
			cd/Vector3D.cpp						\
			CD.cpp


OBJ_CORE    =	$(SRC_CORE:.cpp=.o)

CXXFLAGS+=	-Wall -Wextra -std=c++17 -O2

NAME_CORE   =	Harness-$(CXX)


$(NAME_CORE):	$(OBJ_CORE)
	$(CXX) -o $(NAME_CORE) $(OBJ_CORE) $(CXXFLAGS) -ldl 

all:	$(NAME_CORE) clean

core:	$(NAME_CORE) clean


clean:
		rm -f $(OBJ_CORE)

fclean:		clean
		rm -f $(NAME_CORE)

re:		fclean all