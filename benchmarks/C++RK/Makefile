SRC_CORE=som/Random.cpp						\
			Bounce.cpp							\
			Run.cpp 							\
			main.cpp							\
			List.cpp							\
			Mandelbrot.cpp						\
			Sieve.cpp							\
			Permute.cpp							\
			Queens.cpp							\
			NBody.cpp							\
			Json.cpp							\
			Havlak.cpp							\
			DeltaBlue.cpp		\
			Richards.cpp						\
			Storage.cpp							\
			Towers.cpp							\
			CD.cpp


OBJ_CORE    =	$(SRC_CORE:.cpp=.o)

CXXFLAGS+=	-Wall -Wextra -std=c++17 -O2

NAME_CORE   =	Harness


$(NAME_CORE):	$(OBJ_CORE)
	$(CXX) -o $(NAME_CORE) $(OBJ_CORE) $(CXXFLAGS) -ldl 

all:	$(NAME_CORE) clean

core:	$(NAME_CORE) clean


clean:
		rm -f $(OBJ_CORE)

fclean:		clean
		rm -f $(NAME_CORE)

re:		fclean all